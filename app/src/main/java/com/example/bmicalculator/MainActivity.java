package com.example.bmicalculator;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText etWeight;
    private EditText etHeight;
    private TextView tvBmiValue;
    private TextView tvBmiCategory;

    private static final String KEY_WEIGHT = "key_weight";
    private static final String KEY_HEIGHT = "key_height";
    private static final String KEY_HAS_RESULT = "key_has_result";

    private final DecimalFormat bmiFormatter = new DecimalFormat("#,##0.#");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        AppCompatButton btnCalculate = findViewById(R.id.btnCalculate);
        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiCategory = findViewById(R.id.tvBmiCategory);

        // Limit both fields to max 8 digits total, 2 digits after the decimal point
        InputFilter[] decimalFilter = new InputFilter[]{new DecimalDigitsInputFilter(8, 2)};
        etWeight.setFilters(decimalFilter);
        etHeight.setFilters(decimalFilter);

        btnCalculate.setOnClickListener(v -> calculateBmi());

        // Restore state after rotation (portrait <-> landscape use different layout files,
        // so the Activity is recreated and views must be repopulated)
        if (savedInstanceState != null) {
            String weight = savedInstanceState.getString(KEY_WEIGHT, "");
            String height = savedInstanceState.getString(KEY_HEIGHT, "");
            etWeight.setText(weight);
            etHeight.setText(height);
            if (savedInstanceState.getBoolean(KEY_HAS_RESULT, false)) {
                calculateBmi();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String weight = etWeight.getText().toString();
        String height = etHeight.getText().toString();
        outState.putString(KEY_WEIGHT, weight);
        outState.putString(KEY_HEIGHT, height);
        outState.putBoolean(KEY_HAS_RESULT, !weight.isEmpty() && !height.isEmpty());
    }

    private void calculateBmi() {
        String sWeight = etWeight.getText().toString().trim();
        String sHeight = etHeight.getText().toString().trim();

        if (sWeight.isEmpty() || sHeight.isEmpty()) {
            Toast.makeText(this, R.string.error_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        double weightKg;
        double heightCm;
        try {
            weightKg = Double.parseDouble(sWeight);
            heightCm = Double.parseDouble(sHeight);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.error_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        if (heightCm <= 0) {
            Toast.makeText(this, R.string.error_height_zero, Toast.LENGTH_SHORT).show();
            return;
        }

        double heightM = heightCm / 100.0;
        double bmi = weightKg / (heightM * heightM);

        // "Result : " + formatter.format(...) pattern from the assignment spec
        String bmiText = bmiFormatter.format(bmi);
        tvBmiValue.setText(bmiText);

        applyCategory(bmi);
    }

    private void applyCategory(double bmi) {
        int labelResId;
        int colorResId;

        if (bmi < 18.5) {
            labelResId = R.string.bmi_underweight;
            colorResId = R.color.risk_underweight;
        } else if (bmi < 25.0) {
            labelResId = R.string.bmi_normal;
            colorResId = R.color.risk_normal;
        } else if (bmi < 30.0) {
            labelResId = R.string.bmi_overweight;
            colorResId = R.color.risk_overweight;
        } else {
            labelResId = R.string.bmi_obese;
            colorResId = R.color.risk_obese;
        }

        tvBmiCategory.setText(labelResId);
        tvBmiCategory.setTextColor(ContextCompat.getColor(this, colorResId));
    }

    /**
     * Restricts an EditText to at most `digits` integer digits and
     * `digitsAfterZero` digits after the decimal point.
     */
    private static class DecimalDigitsInputFilter implements InputFilter {
        private final Pattern mPattern;

        DecimalDigitsInputFilter(int digits, int digitsAfterZero) {
            mPattern = Pattern.compile("[0-9]{0," + (digits - 1) + "}+((\\.[0-9]{0,"
                    + (digitsAfterZero - 1) + "})?)|(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder(dest);
            builder.replace(dstart, dend, source.subSequence(start, end).toString());
            Matcher matcher = mPattern.matcher(builder.toString());
            if (!matcher.matches()) {
                return "";
            }
            return null;
        }
    }
}
