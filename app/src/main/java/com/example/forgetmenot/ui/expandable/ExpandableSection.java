package com.example.forgetmenot.ui.expandable;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class ExpandableSection implements Parcelable {

    private final String title;
    private final String body;

    public ExpandableSection(@NonNull String title, @NonNull String body) {
        this.title = title;
        this.body = body;
    }

    protected ExpandableSection(Parcel in) {
        title = in.readString();
        body = in.readString();
    }

    public static final Creator<ExpandableSection> CREATOR = new Creator<ExpandableSection>() {
        @Override
        public ExpandableSection createFromParcel(Parcel in) {
            return new ExpandableSection(in);
        }

        @Override
        public ExpandableSection[] newArray(int size) {
            return new ExpandableSection[size];
        }
    };

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getBody() {
        return body;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(body);
    }
}
