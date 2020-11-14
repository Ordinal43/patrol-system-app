package com.patrolsystemapp.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.patrolsystemapp.CustomLayouts.SquareImageView;
import com.patrolsystemapp.Dialogs.CancelConfirmDialog;
import com.patrolsystemapp.Models.Scan;
import com.patrolsystemapp.Models.Schedule;
import com.patrolsystemapp.Models.Status;
import com.patrolsystemapp.R;
import com.patrolsystemapp.apis.NetworkClient;
import com.patrolsystemapp.apis.UploadApis;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ConfirmShiftActivity extends AppCompatActivity implements View.OnClickListener, CancelConfirmDialog.CancelUploadDialogListener {
    private static final String TAG = "ConfirmShiftActivity";
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

    private List<Status> statusList;
    private ArrayAdapter<Status> statusAdapter;
    private Button btnRetryGetStatus;

    Handler handler = new Handler();
    Runnable runnable = null;

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
        btnRetryGetStatus = findViewById(R.id.btnRetryGetStatus);
        btnRetryGetStatus.setOnClickListener(v -> {
            getListStatus();
        });

        btnRetryGetStatus.setVisibility(View.GONE);

        statusList = new ArrayList<>();
        statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statusList);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnStatus.setAdapter(statusAdapter);
        getListStatus();

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
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<MultipartBody.Part> param_list_images = new ArrayList<>();
        List<MultipartBody.Part> param_list_time = new ArrayList<>();
        int idx = 0;
        for (File file : listFiles) {
            if (file.exists()) {
                RequestBody requestBody = RequestBody.create(file, MediaType.parse("image/*"));
                MultipartBody.Part image = MultipartBody.Part.createFormData("photos[" + idx + "][file]", file.getName(), requestBody);
                param_list_images.add(image);

                MultipartBody.Part time = MultipartBody.Part.createFormData("photos[" + idx + "][photo_time]", df.format(new Date(file.lastModified())));
                param_list_time.add(time);
            }
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
                param_list_time,
                param_token,
                param_id,
                param_message,
                param_status_node_id
        );

        call.enqueue(callback);

        Intent intent = new Intent(this, FinishConfirmActivity.class);
        startActivity(intent);
        finish();
    }

    private void getListStatus() {
        String param_token = sharedPrefs.getString("token", "");

        Retrofit retrofit = NetworkClient.getRetrofit(this);
        UploadApis uploadApis = retrofit.create(UploadApis.class);

        Call<JsonObject> call = uploadApis.getListStatus(param_token);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                assert response.body() != null;
                String jsonString = response.body().toString();
                try {
                    JSONObject obj = new JSONObject(jsonString);
                    System.out.println(obj.toString(2));
                    boolean isErr = (Boolean) obj.get("error");

                    if (!isErr) {
                        setListStatus(obj.getJSONObject("data").getJSONArray("status_node"));
                    } else {
                        throw new Exception("Error API!");
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Daftar kondisi lokasi gagal diambil! silahkan coba lagi", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    spnStatus.setVisibility(View.GONE);
                    btnRetryGetStatus.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                t.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Daftar kondisi lokasi gagal diambil! silahkan coba lagi", Toast.LENGTH_LONG).show();
                    spnStatus.setVisibility(View.GONE);
                    btnRetryGetStatus.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    public void setListStatus(JSONArray listStatus) throws JSONException {
        statusList.clear();
        Gson gson = new GsonBuilder().create();

        if (listStatus.length() > 0) {
            for (int i = 0; i < listStatus.length(); i++) {
                JSONObject row = listStatus.getJSONObject(i);
                Status status = gson.fromJson(row.toString(), Status.class);
                statusList.add(status);
            }
            statusAdapter.notifyDataSetChanged();
            spnStatus.setVisibility(View.VISIBLE);
            btnRetryGetStatus.setVisibility(View.GONE);
        }
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

    Callback<JsonObject> callback = new Callback<JsonObject>() {
        @Override
        public void onResponse(@NotNull Call<JsonObject> call,
                               @NotNull Response<JsonObject> response) {

            if (runnable != null) handler.removeCallbacks(runnable);
            try {
                assert response.body() != null;
                String jsonString = response.body().toString();
                JSONObject obj = new JSONObject(jsonString);
                System.out.println(obj.toString(2));
                boolean isErr = (Boolean) obj.get("error");

                if (isErr) {
                    // Move to another page? show notif?
                    Toast.makeText(getApplicationContext(), "Proses konfirmasi gagal! silahkan ulang proses scan kembali.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Terjadi kesalahan internal pada sistem! Silahkan hubungi admin.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(@NotNull Call<JsonObject> call,
                              @NotNull Throwable t) {
            if (runnable != null) handler.removeCallbacks(runnable);
            Log.d(TAG, "Retrying...");
            retry(call);
            t.printStackTrace();
        }
    };

    private void retry(Call<JsonObject> call) {
        handler.postDelayed(runnable = () -> {
            call.clone().enqueue(callback);
        }, 30 * 1000);
    }
}
