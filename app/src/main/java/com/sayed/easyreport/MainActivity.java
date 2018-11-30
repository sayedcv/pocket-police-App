package com.sayed.easyreport;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.sayed.easyreport.models.Upload;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    final int RC_TAKE_PHOTO = 1;


    private Uri mImageUri;
    ImageView showIV;
    LinearLayout parentContainer;
    CircleImageView imgChooserBtn;
    private ProgressBar mProgressBar;
    Dialog myDialog;
    Bitmap photo;

    //not using file
    File file;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    double longitude ;
    double latitude ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showIV = findViewById(R.id.img_selected);
        imgChooserBtn = findViewById(R.id.img_chooser);
        mProgressBar = findViewById(R.id.progress_bar);
        parentContainer = findViewById(R.id.parent_container);

        myDialog = new Dialog(this);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("PocketPolice");

    }

    public void emergencyCall(View v){
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "0172638493832"));
            startActivity(intent);

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)this, Manifest.permission.CALL_PHONE)) {

                Snackbar.make(v,
                        "Needs Call Permission",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 7);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, 7);
            }
        }
    }

    public void askForLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            longitude = location.getLongitude();
            latitude = location.getLatitude();

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Needs Contacts write permission",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 6);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 6);
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickActionMethod(View v){

        if (v.getId() == R.id.img_chooser){
            openFileChooser();
        }else if(v.getId() == R.id.camera_btn){
            openCamera();
        }else if(v.getId() == R.id.submit_btn){

            TextView txtclose;
            CircleImageView sendBtn;
            LinearLayout parent;
            final EditText detailEt,phonenumEt,witnessPhoneNumEt;


            myDialog.setContentView(R.layout.layout_report_dialog);
            txtclose = myDialog.findViewById(R.id.txtclose);
            sendBtn = myDialog.findViewById(R.id.send_btn);
            detailEt = myDialog.findViewById(R.id.crime_desc_et);
            phonenumEt = myDialog.findViewById(R.id.phone_number_et);
            witnessPhoneNumEt = myDialog.findViewById(R.id.witness_phone_number_et);
//            parent = new LinearLayout(new ContextThemeWrapper(getApplicationContext(), R.style.MyDialogTheme));


            txtclose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Cancel")
                                .setMessage("Do you Want to cancel the report ?")
                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        myDialog.dismiss();
                                        showIV.setVisibility(View.GONE);

                                        Snackbar.make(parentContainer,"Report has been canceled",Snackbar.LENGTH_INDEFINITE)
                                                .setAction("Ok", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                    }
                                                }).show();

                                    }
                                })
                                .setNegativeButton("No",null)

                                .show();

                    }
                });
                sendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Boolean bol = true;
                        String number = phonenumEt.getText().toString().trim();
                        if (number.equals("")){
                            phonenumEt.setError("Provide Phone Number");
                            bol = false;
                        }
                        String crimeInfo = detailEt.getText().toString().trim();
                        if (crimeInfo.equals("")){
                            detailEt.setError("Provide info");
                            bol = false;
                        }
                        String witnessInfo = witnessPhoneNumEt.getText().toString();
                        if (witnessInfo.equals("")){
                            witnessPhoneNumEt.setError("Provide witness info");
                            bol = false;
                        }
                        if (bol){
                            if (mUploadTask != null && mUploadTask.isInProgress()) {
                                Toast.makeText(MainActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                            } else {
                                askForLocation();
                                uploadFile(number,crimeInfo,witnessInfo,latitude,longitude);

                                myDialog.dismiss();
                                showIV.setVisibility(View.GONE);

                                Intent i = new Intent(MainActivity.this,MapsActivity.class);
                                startActivity(i);
                            }

                        }

                    }
                });

                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2F363F")));
                myDialog.show();
        }
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
//opens camera and takes photo
    public void openCamera(){

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);

    }
//not working ......
    private void takePhoto() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(this.getExternalCacheDir(),
                String.valueOf(System.currentTimeMillis()) + ".jpg");
        mImageUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(intent, RC_TAKE_PHOTO);

    }



        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            showIV.setVisibility(View.VISIBLE);
            Picasso.get().load(mImageUri).into(showIV);
        }

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
//            mImageUri = getImageUri(this, photo);
            showIV.setVisibility(View.VISIBLE);
            showIV.setImageBitmap(photo);
//            Picasso.get().load(mImageUri).into(showIV);
        }

    }

    //Not working......gets image uri from Bitmap
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(final String number, final String crimeInfo, final String witnessInfo, final Double lat, final Double lon) {
        mProgressBar.setVisibility(View.VISIBLE);
        if (mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            }, 200);

                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String linkOfDownload = ""+uri+"";

                                    Upload upload = new Upload("imageName",linkOfDownload,number,crimeInfo,witnessInfo,lat,lon);
                                    String uploadId = mDatabaseRef.push().getKey();
                                    mDatabaseRef.child(uploadId).setValue(upload);

                                    Snackbar.make(parentContainer,"Your case has been filed, you will be contacted soon",Snackbar.LENGTH_INDEFINITE)
                                            .setAction("Ok", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    mProgressBar.setVisibility(View.GONE);
                                                }
                                            }).show();

                                    Log.i("donwoad liink of image",uri.toString());
                                }
                            });

                        }
                    })
//                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Uri> task) {
//                            if (task.isSuccessful()){
//                                Uri downUri = task.getResult();
//                                Log.d(TAG, "onComplete: Url: "+ downUri.toString());
//                            }
//                        }})
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);
                        }
                    });
        }else if(photo != null ){

            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + "camera.JPEG");

            // Get the data from an ImageView as bytes
//            imageView.setDrawingCacheEnabled(true);
//            imageView.buildDrawingCache();
//            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = fileReference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(0);
                        }
                    }, 200);

                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String linkOfDownload = ""+uri+"";

                            Upload upload = new Upload("imageName",linkOfDownload,number,crimeInfo,witnessInfo,lat,lon);
                            String uploadId = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadId).setValue(upload);

                            Snackbar.make(parentContainer,"Your case has been filed, you will be contacted soon",Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Ok", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    }).show();

                            Log.i("donwoad liink of image",uri.toString());
                        }
                    });

                }
            })
              .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                  double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                  mProgressBar.setProgress((int) progress);
                }
            });

        }
        else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }

    }

}
