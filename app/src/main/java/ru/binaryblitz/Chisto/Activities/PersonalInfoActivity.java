package ru.binaryblitz.Chisto.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.fabric.sdk.android.Fabric;
import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Model.User;
import ru.binaryblitz.Chisto.R;

public class PersonalInfoActivity extends BaseActivity {

    private MaterialEditText name;
    private MaterialEditText lastname;
    private MaterialEditText city;
    private MaterialEditText street;
    private MaterialEditText house;
    private MaterialEditText flat;
    private MaterialEditText comment;
    private MaterialEditText phone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_contact_info);

        name = (MaterialEditText) findViewById(R.id.name_text);
        lastname = (MaterialEditText) findViewById(R.id.lastname_text);
        city = (MaterialEditText) findViewById(R.id.city_text);
        street = (MaterialEditText) findViewById(R.id.street_text);
        house = (MaterialEditText) findViewById(R.id.house_text);
        flat = (MaterialEditText) findViewById(R.id.flat_text);
        phone = (MaterialEditText) findViewById(R.id.phone);
        comment = (MaterialEditText) findViewById(R.id.comment_text);
    }

    private void validateFields() {
    }
}
