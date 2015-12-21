package at.dingbat.apiutils;

import android.location.Location;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by Max on 11/21/2015.
 */
public class ApiUtil {

    public static final String DEFAULT_TEMPLATE = "{\"typo\":{\"version\":\"1.0\"},\"location\":[],\"master\":{\"default\":{\"size\":10,\"color\":\"#000000\",\"indentation\":0},\"display1\":{\"title\":\"Display 1\",\"size\":34,\"color\":\"#000000\",\"indentation\":0},\"headline\":{\"title\":\"Headline\",\"size\":24,\"color\":\"#000000\",\"indentation\":0},\"title\":{\"title\":\"Title\",\"size\":20,\"color\":\"#000000\",\"indentation\":0},\"subheading\":{\"title\":\"Subheading\",\"size\":16,\"color\":\"#000000\",\"indentation\":0},\"body\":{\"title\":\"Body\",\"size\":14,\"color\":\"#000000\",\"indentation\":0}},\"root\":[]}";

    public static void silentLogin(GoogleApiClient client, final LoginCallback callback) {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(client);
        if(opr.isDone()) {
            callback.onLoggedIn(opr.get());
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    callback.onLoggedIn(googleSignInResult);
                }
            });
        }
    }

    public static void getFolderContents(GoogleApiClient client, DriveFolder folder, final FolderContentsLoadedCallback callback) {
        folder.listChildren(client).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult result) {
                callback.onFolderContentsLoaded(result);
            }
        });
    }

    public static void getMetadata(GoogleApiClient client, DriveFolder folder, final MetadataLoadedCallback callback) {
        folder.getMetadata(client).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
            @Override
            public void onResult(DriveResource.MetadataResult metadataResult) {
                callback.onMetadataLoaded(metadataResult);
            }
        });
    }

    public static void getMetadata(GoogleApiClient client, DriveFile file, final MetadataLoadedCallback callback) {
        file.getMetadata(client).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
            @Override
            public void onResult(DriveResource.MetadataResult metadataResult) {
                callback.onMetadataLoaded(metadataResult);
            }
        });
    }

    public static void createFolder(final GoogleApiClient client, final String title, final DriveFolder folder, final FolderCreatedCallback callback) {
        MetadataChangeSet f = new MetadataChangeSet.Builder()
                .setTitle(title)
                .build();
        folder.createFolder(client, f).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(DriveFolder.DriveFolderResult driveFolderResult) {
                callback.onFolderCreated(driveFolderResult.getDriveFolder());
            }
        });
    }

    public static void createFile(final GoogleApiClient client, final String title, final DriveFolder folder, final Location location, final FileCreatedCallback callback) {
        Drive.DriveApi.newDriveContents(client).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                MetadataChangeSet file = null;

                if(location != null) file = new MetadataChangeSet.Builder()
                        .setTitle(title)
                        .setMimeType("text/typo")
                        .setCustomProperty(new CustomPropertyKey("lat", CustomPropertyKey.PUBLIC), String.valueOf(location.getLatitude()))
                        .setCustomProperty(new CustomPropertyKey("lon", CustomPropertyKey.PUBLIC), String.valueOf(location.getLongitude()))
                        .build();
                else file = new MetadataChangeSet.Builder()
                        .setTitle(title)
                        .setMimeType("text/typo")
                        .build();

                folder.createFile(client, file, result.getDriveContents()).setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(final DriveFolder.DriveFileResult driveFileResult) {
                        writeFile(client, driveFileResult.getDriveFile(), DEFAULT_TEMPLATE, new DocumentSavedCallback() {
                            @Override
                            public void onSaved() {
                                callback.onFileCreated(driveFileResult.getDriveFile());
                            }
                        });
                    }
                });
            }
        });
    }

    public static void readFile(final GoogleApiClient client, DriveFile file, final FileReadCallback callback) {
        file.open(client, DriveFile.MODE_READ_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                DriveContents contents = driveContentsResult.getDriveContents();
                BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));
                final StringBuilder builder = new StringBuilder();
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    callback.onFileRead(builder.toString());
                }
            }
        });
    }

    public static void writeFile(final GoogleApiClient client, DriveFile file, final String json, final DocumentSavedCallback callback) {
        file.open(client, DriveFile.MODE_READ_WRITE, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                DriveContents contents = driveContentsResult.getDriveContents();
                ParcelFileDescriptor parcelFileDescriptor = contents.getParcelFileDescriptor();
                FileOutputStream out = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
                try {
                    out.getChannel().truncate(0);
                    out.write(json.getBytes());
                    callback.onSaved();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                contents.commit(client, null);
            }
        });
    }

    public static void renameFile(final GoogleApiClient client, final DriveFile file, final String title) {
        file.getMetadata(client).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
            @Override
            public void onResult(DriveResource.MetadataResult metadataResult) {
                MetadataChangeSet set = new MetadataChangeSet.Builder().setTitle(title).build();
                file.updateMetadata(client, set);
            }
        });
    }

    public static void addLocation(final GoogleApiClient client, final DriveFile file, final Location location) {
        file.getMetadata(client).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
            @Override
            public void onResult(DriveResource.MetadataResult metadataResult) {
                MetadataChangeSet set = new MetadataChangeSet.Builder()
                        .setCustomProperty(new CustomPropertyKey("lat", CustomPropertyKey.PUBLIC), String.valueOf(location.getLatitude()))
                        .setCustomProperty(new CustomPropertyKey("lon", CustomPropertyKey.PUBLIC), String.valueOf(location.getLongitude()))
                        .build();
                file.updateMetadata(client, set);
            }
        });
    }

    public static void removeLocation(final GoogleApiClient client, final DriveFile file) {
        file.getMetadata(client).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
            @Override
            public void onResult(DriveResource.MetadataResult metadataResult) {
                MetadataChangeSet set = new MetadataChangeSet.Builder()
                        .deleteCustomProperty(new CustomPropertyKey("lat", CustomPropertyKey.PUBLIC))
                        .deleteCustomProperty(new CustomPropertyKey("lon", CustomPropertyKey.PUBLIC))
                        .build();
                file.updateMetadata(client, set);
            }
        });
    }

    public static void delete(GoogleApiClient client, String id, final DeletedCallback callback) {
        DriveId driveid = DriveId.decodeFromString(id);
        ResultCallback c = new ResultCallback() {
            @Override
            public void onResult(Result result) {
                callback.onDeleted();
            }
        };

        if(driveid.getResourceType() == DriveId.RESOURCE_TYPE_FILE) Drive.DriveApi.getFile(client, driveid).delete(client).setResultCallback(c);
        else Drive.DriveApi.getFolder(client, driveid).delete(client).setResultCallback(c);

    }

    public static void search(final GoogleApiClient client, String title, final SearchCallback callback) {
        Query query = new Query.Builder()
                .addFilter(Filters.and(
                        Filters.eq(SearchableField.MIME_TYPE, "text/typo"),
                        Filters.contains(SearchableField.TITLE, title)
                )).build();
        Drive.DriveApi.query(client, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult result) {
                callback.onResult(result);
            }
        });
    }

    public static void getAllTypos(final GoogleApiClient client, final SearchCallback callback) {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/typo"))
                .setSortOrder(new SortOrder.Builder().addSortDescending(SortableField.MODIFIED_DATE).build())
                .build();

        Drive.DriveApi.query(client, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                callback.onResult(metadataBufferResult);
            }
        });
    }

    public static interface LoginCallback {
        void onLoggedIn(GoogleSignInResult result);
    }

    public static interface FolderContentsLoadedCallback {
        void onFolderContentsLoaded(DriveApi.MetadataBufferResult result);
    }

    public static interface FolderCreatedCallback {
        void onFolderCreated(DriveFolder folder);
    }

    public static interface FileCreatedCallback {
        void onFileCreated(DriveFile file);
    }

    public static interface FileReadCallback {
        void onFileRead(String content);
    }

    public static interface SearchCallback {
        void onResult(DriveApi.MetadataBufferResult result);
    }

    public static interface MetadataLoadedCallback {
        void onMetadataLoaded(DriveResource.MetadataResult result);
    }

    public static interface DocumentSavedCallback {
        void onSaved();
    }

    public static interface DeletedCallback {
        void onDeleted();
    }

}
