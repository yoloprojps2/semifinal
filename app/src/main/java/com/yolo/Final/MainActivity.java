package com.yolo.Final;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;


public class MainActivity extends AppCompatActivity {


    private Camera mCamera;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
        };

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }


       initializeCamera();


    }


    private void initializeCamera(){

        mCamera = Camera.open(1);
        SurfaceTexture surfaceTexture = new SurfaceTexture(10);
        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.startPreview();
        Camera.Parameters params = mCamera.getParameters();
        params.setJpegQuality(100);

        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Log.i("ratio"+sizes,"bknlm");
        params.setPictureSize(sizes.get(0).width,sizes.get(0).height);
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(params);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        takePic.run();
    }

        private Runnable takePic = new Runnable() {
            @Override
            public void run() {
                SurfaceTexture surfaceTexture = new SurfaceTexture(10);
                try {
                    mCamera.setPreviewTexture(surfaceTexture);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mCamera.startPreview();
                mCamera.takePicture(null, null, mPicture);
                mHandler.postDelayed(this, 12000);
            }
        };




    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
        uploadMultipart();
        }
    };





    private static File getOutputMediaFile() {
        DeleteFiles();
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }




    /*
     * This is the method responsible for image upload
     * We need the full image path and the name for the image in this method
     * */
    public void uploadMultipart() {
        //getting name for the image
        //getting the actual path of the image
        String path;
        File directory  =new File(Environment.getExternalStorageDirectory().toString() + "/Pictures/MyCameraApp");
        File[] file = directory.listFiles();
        path =  file[0].getAbsolutePath();



        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, Constants.UPLOAD_URL)
                    .addFileToUpload(path, "image") //Adding file
                     //Adding text parameter to the request
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload
            Log.e("upload","yes it dit try");

        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }




    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private static void  DeleteFiles() {
        String path = Environment.getExternalStorageDirectory().toString() + "/Pictures/MyCameraApp";
        File dir = new File(path);
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}