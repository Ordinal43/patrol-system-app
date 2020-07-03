package com.patrolsystemapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;
import com.patrolsystemapp.Apis.ConfirmShiftCall;
import com.patrolsystemapp.Apis.NetworkClient;
import com.patrolsystemapp.Apis.UploadApis;
import com.patrolsystemapp.CustomLayout.SquareImageView;
import com.patrolsystemapp.Dialog.CancelConfirmDialog;
import com.patrolsystemapp.Model.Scan;
import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.Model.Status;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class ConfirmShiftActivity extends AppCompatActivity implements View.OnClickListener, CancelConfirmDialog.CancelUploadDialogListener {
    private float CURRENT_DENSITY;
    Schedule matchedSchedule;

    private SharedPreferences sharedPrefs;

    // shift not done layouts
    private LinearLayout linearLayoutConfirmShift;
    private LinearLayout linearLayoutTakePhotos;
    private Spinner spnStatus;
    private EditText edtMessage;

    private SquareImageView imagePreview;
    private int[] arrIdThumbnail = {
            R.id.imageThumbnail_1,
            R.id.imageThumbnail_2,
            R.id.imageThumbnail_3,
            R.id.imageThumbnail_4
    };

    private FloatingActionButton btnDeleteImage;
    private Drawable errorImageDrawable;
    private Drawable loadingImageDrawable;

    private ArrayList<File> listFiles = new ArrayList<>();
    private File currentFile = null;

    @Override
    public void onBackPressed() {
        openDialog();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_shift);
        matchedSchedule = (Schedule) getIntent().getSerializableExtra("matchedSchedule");

        initWidgets();

        linearLayoutConfirmShift.setVisibility(View.VISIBLE);
        linearLayoutTakePhotos.setVisibility(View.VISIBLE);
    }

    private void initWidgets() {
        sharedPrefs = getSharedPreferences("patrol_app", Context.MODE_PRIVATE);

        CURRENT_DENSITY = getResources().getDisplayMetrics().density;
        linearLayoutConfirmShift = findViewById(R.id.layoutConfirmShift);
        linearLayoutConfirmShift.setVisibility(View.GONE);

        TextView txtMatchedRoom = findViewById(R.id.txtMatchedRoom);
        TextView txtMatchedTime = findViewById(R.id.txtMatchedTime);
        TextView txtMatchedCounts = findViewById(R.id.txtMatchedCounts);

        txtMatchedRoom.setText(matchedSchedule.getRoom());
        String times = matchedSchedule.getTime_start() + " - " + matchedSchedule.getTime_end();
        txtMatchedTime.setText(times);

        String status;
        int countScanned = Integer.parseInt(matchedSchedule.getCountScanned());
        if (countScanned == 0) {
            status = "Belum diperiksa";
        } else {
            Scan lastScan = matchedSchedule.getLast_scan();
            status = "Diperiksa " + countScanned + " kali (terakhir: " + lastScan.getScan_time() + ")";
        }
        txtMatchedCounts.setText(status);

        linearLayoutTakePhotos = findViewById(R.id.linearLayoutTakePhotos);
        linearLayoutTakePhotos.setVisibility(View.GONE);

        spnStatus = findViewById(R.id.spnStatus);
        List<Status> statusList = new ArrayList<>();

        Status aman = new Status("1", "Aman");
        Status mencurigakan = new Status("2", "Mencurigakan");
        Status tdkAman = new Status("3", "Tidak Aman");

        statusList.add(aman);
        statusList.add(mencurigakan);
        statusList.add(tdkAman);

        ArrayAdapter<Status> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statusList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnStatus.setAdapter(adapter);

        edtMessage = findViewById(R.id.edtMessage);

        imagePreview = findViewById(R.id.uploadedImagePreview);
        ImageView imageThumbnail_1 = findViewById(R.id.imageThumbnail_1);
        imageThumbnail_1.setOnClickListener(this);

        ImageView imageThumbnail_2 = findViewById(R.id.imageThumbnail_2);
        imageThumbnail_2.setOnClickListener(this);

        ImageView imageThumbnail_3 = findViewById(R.id.imageThumbnail_3);
        imageThumbnail_3.setOnClickListener(this);

        ImageView imageThumbnail_4 = findViewById(R.id.imageThumbnail_4);
        imageThumbnail_4.setOnClickListener(this);

        errorImageDrawable = getDrawable(R.drawable.broken_image);
        loadingImageDrawable = getDrawable(R.drawable.loading_image);

        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        btnDeleteImage.hide();
        btnDeleteImage.setOnClickListener(v -> deleteImage());

        Button btnConfirmShift = findViewById(R.id.btnConfirmShift);
        btnConfirmShift.setOnClickListener(v -> {
            if (listFiles.isEmpty()) {
                Toast.makeText(this, "Belum ada gambar!", Toast.LENGTH_LONG).show();
            } else {
                uploadConfirmation();
            }
        });

        Button btnCancelConfirmShift = findViewById(R.id.btnCancelConfirmShift);
        btnCancelConfirmShift.setOnClickListener(v -> openDialog());
    }

    @Override
    public void onClick(View v) {
        // cast to ImageView
        ImageView thumbnail = (ImageView) v;

        if (thumbnail != null && thumbnail.getDrawable() != null) {
            // if ImageView tag is null, open ImagePicker
            if (thumbnail.getTag() == null) {
                ImagePicker.Companion.with(this)
                        .compress(512)
                        .maxResultSize(1920, 1920)
//                        .maxResultSize(620, 620)
                        .start();
            } else {
                currentFile = (File) thumbnail.getTag();
                Uri imageUri = Uri.fromFile(currentFile);
                int width = (int) (imagePreview.getWidth() * CURRENT_DENSITY);
                int height = (int) (imagePreview.getHeight() * CURRENT_DENSITY);

                Picasso.get()
                        .load(imageUri)
                        .placeholder(loadingImageDrawable)
                        .error(errorImageDrawable)
                        .resize(width, height)
                        .centerCrop()
                        .into(imagePreview);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //You can get File object from intent
            currentFile = ImagePicker.Companion.getFile(data);
            Uri currentUri = Uri.fromFile(currentFile);

            int width = (int) (imagePreview.getWidth() * CURRENT_DENSITY);
            int height = (int) (imagePreview.getHeight() * CURRENT_DENSITY);
            Picasso.get()
                    .load(currentUri)
                    .placeholder(loadingImageDrawable)
                    .error(errorImageDrawable)
                    .resize(width, height)
                    .centerCrop()
                    .into(imagePreview);

            for (int id : arrIdThumbnail) {
                ImageView currentImage = findViewById(id);
                width = (int) (currentImage.getWidth() * CURRENT_DENSITY);
                height = (int) (currentImage.getHeight() * CURRENT_DENSITY);
                if (currentImage.getTag() == null) {
                    currentImage.setTag(currentFile);
                    Picasso.get()
                            .load(currentUri)
                            .placeholder(loadingImageDrawable)
                            .error(errorImageDrawable)
                            .resize(width, height)
                            .centerCrop()
                            .into(currentImage);
                    break;
                }
            }

            String filePath = ImagePicker.Companion.getFilePath(data);
            // DO NOT use currentFile since it will add the reference instead
            assert filePath != null;
            listFiles.add(new File(filePath));

            if (!btnDeleteImage.isShown()) {
                btnDeleteImage.show();
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.Companion.getError(data), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteImage() {
        if (currentFile != null) {
            int idx = 0;
            for (int id : arrIdThumbnail) {
                ImageView iterator = findViewById(id);
                if (currentFile.equals(iterator.getTag())) {
                    // reset the tag
                    iterator.setTag(null);
                    listFiles.get(idx).delete();
                    listFiles.remove(idx);
                    break;
                }
                idx++;
            }

            // rearrange thumbnails
            int thumbnailLength = 4;
            for (int i = idx; i < thumbnailLength; i++) {
                ImageView current = findViewById(arrIdThumbnail[i]);
                int width = (int) (current.getWidth() * CURRENT_DENSITY);
                int height = (int) (current.getHeight() * CURRENT_DENSITY);

                // if its the last thumbnail, replace it add_image drawable
                if (i == thumbnailLength - 1) {
                    Picasso.get()
                            .load(R.drawable.add_image)
                            .placeholder(loadingImageDrawable)
                            .error(errorImageDrawable)
                            .resize(width, height)
                            .centerCrop()
                            .into(current);
                    current.setTag(null);
                } else {
                    ImageView next = findViewById(arrIdThumbnail[i + 1]);

                    // if the next thumbnails image is empty
                    if (next.getTag() == null) {
                        Picasso.get()
                                .load(R.drawable.add_image)
                                .placeholder(loadingImageDrawable)
                                .error(errorImageDrawable)
                                .resize(width, height)
                                .centerCrop()
                                .into(current);
                        current.setTag(null);
                        break;
                    } else {
                        Uri imageUri = Uri.fromFile((File) next.getTag());
                        Picasso.get()
                                .load(imageUri)
                                .placeholder(loadingImageDrawable)
                                .error(errorImageDrawable)
                                .resize(width, height)
                                .centerCrop()
                                .into(current);
                        current.setTag(next.getTag());
                    }
                }
            }

            int width = (int) (imagePreview.getWidth() * CURRENT_DENSITY);
            int height = (int) (imagePreview.getHeight() * CURRENT_DENSITY);

            if (listFiles.isEmpty()) {
                btnDeleteImage.hide();
                Picasso.get()
                        .load(R.drawable.empty_image)
                        .placeholder(loadingImageDrawable)
                        .error(errorImageDrawable)
                        .resize(width, height)
                        .centerCrop()
                        .into(imagePreview);
                currentFile = null;
            } else {
                if (idx > listFiles.size() - 1) {
                    idx = listFiles.size() - 1;
                }
                currentFile = (File) findViewById(arrIdThumbnail[idx]).getTag();
                Uri imageUri = Uri.fromFile((File) findViewById(arrIdThumbnail[idx]).getTag());
                Picasso.get()
                        .load(imageUri)
                        .placeholder(loadingImageDrawable)
                        .error(errorImageDrawable)
                        .resize(width, height)
                        .centerCrop()
                        .into(imagePreview);
            }
        }
    }

    public void uploadConfirmation() {
        Status selectedStatus = (Status) spnStatus.getSelectedItem();
        String statusId = selectedStatus.getId();
        String message = edtMessage.getText().toString();

        List<MultipartBody.Part> param_list_images = new ArrayList<>();
        int idx = 0;
        for (File file : listFiles) {
            RequestBody requestBody = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part image = MultipartBody.Part.createFormData("photos[" + idx + "]", file.getName(), requestBody);
            param_list_images.add(image);
            idx++;
        }

        RequestBody param_token = RequestBody.create(sharedPrefs.getString("token", ""), MediaType.parse("multipart/form-data"));
        RequestBody param_id = RequestBody.create(matchedSchedule.getId(), MediaType.parse("multipart/form-data"));
        RequestBody param_message = RequestBody.create(message, MediaType.parse("multipart/form-data"));
        RequestBody param_status_node_id = RequestBody.create(statusId, MediaType.parse("multipart/form-data"));

        Retrofit retrofit = NetworkClient.getRetrofit(this);
        UploadApis uploadApis = retrofit.create(UploadApis.class);

        Call<JsonObject> call = uploadApis.uploadConfirmation(
                param_list_images,
                param_token,
                param_id,
                param_message,
                param_status_node_id
        );

        ConfirmShiftCall confirmShiftCall = new ConfirmShiftCall(matchedSchedule, call);

        Intent intent = new Intent(this, FinishConfirmActivity.class);
        startActivity(intent);
        finish();
    }

    private void openDialog() {
        CancelConfirmDialog cancelConfirmDialog = new CancelConfirmDialog();
        cancelConfirmDialog.show(getSupportFragmentManager(), "Cancel Upload Dialog");
    }

    @Override
    public void backToHome() {
        finish();
    }

    @Override
    public void closeDialog() {

    }
}