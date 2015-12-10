package at.dingbat.type.util;

import android.os.ParcelFileDescriptor;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import at.dingbat.type.model.Document;

/**
 * Created by Max on 11/21/2015.
 */
public class ApiUtil {

    public static final String DEFAULT_TEMPLATE = "{\"typo\":{\"version\":\"1.0\",},\"location\":[],\"master\":{\"default\":{\"size\":14,\"color\":black},\"title\":{\"size\":56,\"color\":black},\"subtitle\":{\"size\":32,\"color\":grey},\"paragraph\":{\"size\":24,\"color\":black}},\"root\":[],\"images\":{}}";

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

    public static void createFile(final GoogleApiClient client, final String title, final DriveFolder folder, final FileCreatedCallback callback) {
        Drive.DriveApi.newDriveContents(client).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult result) {
                MetadataChangeSet file = new MetadataChangeSet.Builder()
                        .setTitle(title)
                        .setMimeType("text/typo")
                        .build();
                folder.createFile(client, file, result.getDriveContents()).setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(final DriveFolder.DriveFileResult driveFileResult) {
                        driveFileResult.getDriveFile().open(client, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(final DriveApi.DriveContentsResult result) {
                                ParcelFileDescriptor parcelFileDescriptor = result.getDriveContents().getParcelFileDescriptor();
                                FileOutputStream out = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
                                Writer writer = new OutputStreamWriter(out);
                                try {
                                    writer.write(DEFAULT_TEMPLATE);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        out.close();
                                    } catch (IOException e) {
                                    }
                                    result.getDriveContents().commit(client, null).setResultCallback(new ResultCallback<Status>() {
                                        @Override
                                        public void onResult(Status status) {
                                            callback.onFileCreated(driveFileResult.getDriveFile());
                                        }
                                    });
                                }
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

    public static void writeFile(final GoogleApiClient client, DriveFile file, final String json, final Document.DocumentSavedCallback callback) {
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

}
