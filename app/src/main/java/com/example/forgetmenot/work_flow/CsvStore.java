package com.example.forgetmenot.work_flow;

import android.content.Context;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class CsvStore {
    private static final String FILE_NAME = "workflow.csv";

    private static final String COL_CHECKED_IN = "Checked in.";
    private static final String COL_CHECKED_OUT = "Checked out.";

    private CsvStore() {}

    public static File getFile(final Context context) {
        return new File(context.getFilesDir(), FILE_NAME);
    }

    public static String formatLocal(final long timeMillis) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    public static void appendCheckInRow(final Context context, final long roundedMillis) throws Exception {
        final File csv = getFile(context);

        if (!csv.exists()) {
            final List<String> init = new ArrayList<>();
            init.add(COL_CHECKED_IN + "," + COL_CHECKED_OUT);
            Files.write(csv.toPath(), init, StandardCharsets.UTF_8);
        }

        final List<String> lines = new ArrayList<>(Files.readAllLines(csv.toPath(), StandardCharsets.UTF_8));
        if (lines.isEmpty()) throw new IllegalStateException("CSV has no header row.");

        final String[] headers = lines.get(0).split(",", -1);
        final int inIdx = indexOf(headers, COL_CHECKED_IN);
        final int outIdx = indexOf(headers, COL_CHECKED_OUT);
        if (inIdx < 0 || outIdx < 0) throw new IllegalStateException("CSV missing required columns.");

        final String[] row = new String[headers.length];
        for (int i = 0; i < row.length; i++) row[i] = "";

        row[inIdx] = escapeCsv(formatLocal(roundedMillis));
        row[outIdx] = "";

        lines.add(join(row));
        Files.write(csv.toPath(), lines, StandardCharsets.UTF_8);
    }

    public static void updateLastRowCheckOut(final Context context, final long roundedMillis) throws Exception {
        final File csv = getFile(context);
        if (!csv.exists()) throw new IllegalStateException("CSV does not exist yet.");
        final List<String> lines = new ArrayList<>(Files.readAllLines(csv.toPath(), StandardCharsets.UTF_8));
        if (lines.size() < 2) throw new IllegalStateException("CSV has no data rows to update.");

        final String[] headers = lines.get(0).split(",", -1);
        final int outIdx = indexOf(headers, COL_CHECKED_OUT);
        if (outIdx < 0) throw new IllegalStateException("CSV missing required column: " + COL_CHECKED_OUT);

        final int lastIdx = lines.size() - 1;
        final String[] lastRow = ensureSize(lines.get(lastIdx).split(",", -1), headers.length);
        lastRow[outIdx] = escapeCsv(formatLocal(roundedMillis));

        lines.set(lastIdx, join(lastRow));
        Files.write(csv.toPath(), lines, StandardCharsets.UTF_8);
    }

    private static int indexOf(final String[] arr, final String value) {
        for (int i = 0; i < arr.length; i++) if (value.equals(arr[i])) return i;
        return -1;
    }

    private static String[] ensureSize(final String[] row, final int size) {
        final String[] out = new String[size];
        for (int i = 0; i < size; i++) out[i] = (i < row.length) ? row[i] : "";
        return out;
    }

    private static String join(final String[] cells) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(cells[i] == null ? "" : cells[i]);
        }
        return sb.toString();
    }

    private static String escapeCsv(final String value) {
        if (value == null) return "";
        final boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (!needsQuotes) return value;
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
