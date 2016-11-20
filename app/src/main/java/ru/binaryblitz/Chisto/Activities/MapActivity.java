package ru.binaryblitz.Chisto.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import ru.binaryblitz.Chisto.Base.BaseActivity;
import ru.binaryblitz.Chisto.Custom.MyMapFragment;
import ru.binaryblitz.Chisto.R;

public class MapActivity extends BaseActivity implements MyMapFragment.TouchableWrapper.UpdateMapAfterUserInteraction {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
    }

    @Override
    public void onUpdateMapAfterUserInteraction() {

    }
}
