package com.patrolsystemapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.patrolsystemapp.CustomLayout.SquareImageView;
import com.patrolsystemapp.Model.Schedule;
import com.patrolsystemapp.Model.Status;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfirmShiftActivity extends AppCompatActivity implements View.OnClickListener {
    private final int THUMBNAIL_LENGTH = 4;

    Schedule matchedSchedule;

    // shift card
    private TextView txtMatchedRoom;
    private TextView txtMatchedTime;
    private TextView txtMatchedMessage;

    // shift not done layouts
    private LinearLayout linearLayoutConfirmShift;
    private LinearLayout linearLayoutTakePhotos;
    private Spinner spnStatus;
    private EditText edtMessage;

    private SquareImageView imagePreview;
    private ImageView imageThumbnail_1;
    private ImageView imageThumbnail_2;
    private ImageView imageThumbnail_3;
    private ImageView imageThumbnail_4;

    private int[] arrIdThumbnail = {
            R.id.imageThumbnail_1,
            R.id.imageThumbnail_2,
            R.id.imageThumbnail_3,
            R.id.imageThumbnail_4
    };

    private FloatingActionButton btnDeleteImage;
    private Drawable addImageDrawable;

    private ArrayList<File> listFiles = new ArrayList<>();
    private File currentFile = null;

    private Button btnConfirmShift;

    // shift done layouts
    private LinearLayout linearLayoutShiftConfirmed;
    private TextView txtConfirmedOn;
    private Button btnToHome2;

    @Override
    public void onBackPressed() {
        // do nothing
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_shift);
        matchedSchedule = (Schedule) getIntent().getSerializableExtra("matchedSchedule");

        initWidgets();

        // check if schedule is already confirmed previously
        if (Objects.toString(matchedSchedule.getScan_time(), "").isEmpty()) {
            linearLayoutConfirmShift.setVisibility(View.VISIBLE);
            linearLayoutTakePhotos.setVisibility(View.VISIBLE);
            txtMatchedMessage.setVisibility(View.GONE);
        } else {
            linearLayoutShiftConfirmed.setVisibility(View.VISIBLE);
            txtConfirmedOn.setVisibility(View.VISIBLE);
            txtMatchedTime.setVisibility(View.GONE);
        }
    }

    private void initWidgets() {

        txtMatchedRoom = findViewById(R.id.txtMatchedRoom);
        txtMatchedRoom.setText(matchedSchedule.getRoom());

        txtMatchedTime = findViewById(R.id.txtMatchedTime);
        String matchedTime = "(" + matchedSchedule.getTime_start() + " - " + matchedSchedule.getTime_end() + ")";
        txtMatchedTime.setText(matchedTime);

        txtMatchedMessage = findViewById(R.id.txtMatchedMessage);
        String message = "\"" + matchedSchedule.getMessage() + "\"";
        txtMatchedMessage.setText(message);

        linearLayoutConfirmShift = findViewById(R.id.layoutConfirmShift);
        linearLayoutConfirmShift.setVisibility(View.GONE);

        linearLayoutShiftConfirmed = findViewById(R.id.layoutShiftConfirmed);
        linearLayoutShiftConfirmed.setVisibility(View.GONE);

        linearLayoutTakePhotos = findViewById(R.id.linearLayoutTakePhotos);
        linearLayoutTakePhotos.setVisibility(View.GONE);

        txtConfirmedOn = findViewById(R.id.txtConfirmedOn);
        txtConfirmedOn.setVisibility(View.GONE);
        String confirmMessage = "Dikonfirmasi pukul " + matchedSchedule.getScan_time();
        txtConfirmedOn.setText(confirmMessage);

        spnStatus = findViewById(R.id.spnStatus);
        List<Status> statusList = new ArrayList<>();

        Status aman = new Status("1", "Aman");
        Status mencurigakan = new Status("2", "Mencurigakan");
        Status tdkAman = new Status("3", "Tidak Aman");

        statusList.add(aman);
        statusList.add(mencurigakan);
        statusList.add(tdkAman);

        ArrayAdapter<Status> adapter = new ArrayAdapter<Status>(
                this, android.R.layout.simple_spinner_item, statusList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnStatus.setAdapter(adapter);

        edtMessage = findViewById(R.id.edtMessage);

        imagePreview = findViewById(R.id.uploadedImagePreview);
        imageThumbnail_1 = findViewById(R.id.imageThumbnail_1);
        imageThumbnail_1.setOnClickListener(this);

        imageThumbnail_2 = findViewById(R.id.imageThumbnail_2);
        imageThumbnail_2.setOnClickListener(this);

        imageThumbnail_3 = findViewById(R.id.imageThumbnail_3);
        imageThumbnail_3.setOnClickListener(this);

        imageThumbnail_4 = findViewById(R.id.imageThumbnail_4);
        imageThumbnail_4.setOnClickListener(this);

        addImageDrawable = getResources().getDrawable(R.drawable.add_image);

        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        btnDeleteImage.hide();
        btnDeleteImage.setOnClickListener(v -> {
            deleteImage();
        });

        btnConfirmShift = findViewById(R.id.btnConfirmShift);
        btnConfirmShift.setOnClickListener(v -> {
            if(listFiles.isEmpty()) {
                Toast.makeText(this, "Belum ada gambar!", Toast.LENGTH_LONG).show();
            } else {
                uploadConfirmation();
            }
        });

        btnToHome2 = findViewById(R.id.btnToHome2);
        btnToHome2.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

    }

    @Override
    public void onClick(View v) {
        // cast to ImageView
        ImageView thumbnail = (ImageView) v;

        if (thumbnail != null && thumbnail.getDrawable() != null) {
            // if ImageView src is still using the add_image drawable, open ImagePicker
            if (thumbnail.getDrawable().getConstantState() == addImageDrawable.getConstantState()) {
                ImagePicker.Companion.with(this)
                        .cropSquare()
                        .compress(512)
                        .cameraOnly()
                        .maxResultSize(620, 620)
                        .start();
            } else {
                currentFile = (File) thumbnail.getTag();
                imagePreview.setImageURI(Uri.fromFile(currentFile));
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

            imagePreview.setImageURI(currentUri);

            for (int id : arrIdThumbnail) {
                ImageView current = findViewById(id);
                if (current.getTag() == null) {
                    current.setImageURI(currentUri);
                    current.setTag(currentFile);
                    break;
                }
            }

            String filePath = ImagePicker.Companion.getFilePath(data);
            // DO NOT use currentFile since it will add the reference instead
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
            // loop through the thumbnails and delete matched File
            int idx = 0;
            for (int id : arrIdThumbnail) {
                ImageView iter = findViewById(id);
                if (currentFile.equals(iter.getTag())) {
                    // reset the tag
                    iter.setTag(null);
                    listFiles.get(idx).delete();
                    listFiles.remove(idx);
                    // rearange thumbnails and tags
                    for (int i = idx; i < THUMBNAIL_LENGTH; i++) {
                        ImageView current = findViewById(arrIdThumbnail[i]);
                        if (i == THUMBNAIL_LENGTH - 1) {
                            current.setImageDrawable(addImageDrawable);
                            current.setTag(null);
                        } else {
                            ImageView next = findViewById(arrIdThumbnail[i + 1]);
                            // if ImageView src is still using the add_image drawable, open ImagePicker
                            if (next.getDrawable().getConstantState() == addImageDrawable.getConstantState()) {
                                current.setImageDrawable(addImageDrawable);
                                current.setTag(null);
                                break;
                            } else {
                                current.setImageURI(Uri.fromFile((File) next.getTag()));
                                current.setTag(next.getTag());
                            }
                        }
                    }
                    break;
                }
                idx++;
            }
            if (listFiles.isEmpty()) {
                btnDeleteImage.hide();
                imagePreview.setImageDrawable(getResources().getDrawable(R.drawable.empty_image));
                currentFile = null;
            } else {
                if (idx > listFiles.size() - 1) {
                    idx = listFiles.size() - 1;
                }
                currentFile = (File) findViewById(arrIdThumbnail[idx]).getTag();
                imagePreview.setImageURI(
                        Uri.fromFile((File) findViewById(arrIdThumbnail[idx]).getTag())
                );
            }
        }
    }

    public void uploadConfirmation() {
        Status selectedStatus = (Status) spnStatus.getSelectedItem();
        String statusId = selectedStatus.getId();
        String message = edtMessage.getText().toString();

        Intent intent = new Intent(this, UploadConfirmationActivity.class);
        intent.putExtra("matchedSchedule", matchedSchedule);
        intent.putExtra("listFiles", listFiles);
        intent.putExtra("statusId", statusId);
        intent.putExtra("message", message);

        startActivity(intent);
    }


}