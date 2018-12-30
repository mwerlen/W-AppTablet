package werlen.fr.whatsapptablet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WebContent extends AppCompatActivity {
    private WebView webView;


    private ValueCallback<Uri> asw_file_message;
    private ValueCallback<Uri[]> asw_file_path;
    private final static int asw_file_req = 1;
    private String asw_cam_message;
    private static final String TAG = WebContent.class.getSimpleName();

    private final static int loc_perm = 1;
    private final static int file_perm = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = findViewById(R.id.webView);
        // Try to have a nice view
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(false);
        webView.getSettings().setSupportZoom(true);
        webView.setInitialScale(160);

        // Speed up things
        webView.getSettings().setAppCacheEnabled(true);

        // Enable all capabilities as a  normal browser
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);


        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }

            //Handling input[type="file"] requests for android API 21+
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams){
                get_file();
                if (asw_file_path != null) {
                    asw_file_path.onReceiveValue(null);
                }
                asw_file_path = filePathCallback;
                Intent takePictureIntent = null;
                takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(WebContent.this.getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = create_image();
                        takePictureIntent.putExtra("PhotoPath", asw_cam_message);
                    } catch (IOException ex) {
                        Log.e(TAG, "Image file creation failed", ex);
                    }
                    if (photoFile != null) {
                        asw_cam_message = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.fl_chooser));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, asw_file_req);
                return true;
            }


        });

        // Make our apps looks like a real Chrome
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36 OPR/42.0.2393.9");

        webView.loadUrl("https://web.whatsapp.com/%F0%9F%8C%90/" + Locale.getDefault().getLanguage());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }


    //Checking permission for storage and camera for writing and uploading images
    public void get_file(){
        ActivityCompat.requestPermissions(WebContent.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, file_perm);
        ActivityCompat.requestPermissions(WebContent.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, file_perm);
        ActivityCompat.requestPermissions(WebContent.this, new String[]{Manifest.permission.CAMERA}, file_perm);
    }


    private File create_image() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String file_name    = new SimpleDateFormat("yyyy_mm_ss").format(new Date());
        String new_name     = "file_"+file_name+"_";
        File sd_directory   = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(new_name, ".jpg", sd_directory);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == asw_file_req) {
                    if (null == asw_file_path) {
                        return;
                    }
                    if (intent == null || intent.getData() == null) {
                        if (asw_cam_message != null) {
                            results = new Uri[]{Uri.parse(asw_cam_message)};
                        }
                    } else {
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{ Uri.parse(dataString) };
                        } else {
                            if (intent.getClipData() != null) {
                                final int numSelectedFiles = intent.getClipData().getItemCount();
                                results = new Uri[numSelectedFiles];
                                for (int i = 0; i < numSelectedFiles; i++) {
                                    results[i] = intent.getClipData().getItemAt(i).getUri();
                                }
                            }
                        }
                    }
                }
            }
            asw_file_path.onReceiveValue(results);
            asw_file_path = null;
        } else {
            if (requestCode == asw_file_req) {
                if (null == asw_file_message) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                asw_file_message.onReceiveValue(result);
                asw_file_message = null;
            }
        }
    }
}
