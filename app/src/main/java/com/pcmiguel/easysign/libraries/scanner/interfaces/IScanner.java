package com.pcmiguel.easysign.libraries.scanner.interfaces;

import android.graphics.Bitmap;

import com.pcmiguel.easysign.libraries.scanner.enums.ScanHint;

public interface IScanner {
    void displayHint(ScanHint scanHint);
    void onPictureClicked(Bitmap bitmap);
}
