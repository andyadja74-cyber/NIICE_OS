
package com.niice.os;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    private FrameLayout desktop;
    private TextView clock, battery;
    private Handler handler = new Handler();
    private boolean receiverRegistered = false;
    private String pendingCode = "";

    private final int WHITE = Color.WHITE;
    private final int DARK = Color.rgb(18, 29, 48);
    private final int BLUE = Color.rgb(55, 120, 230);

    private final String[] apps = {
        "Ce PC", "Fichiers", "Terminal", "Calculatrice",
        "Notes", "Paramètres", "Navigateur", "Galerie",
        "Calendrier", "Gestionnaire ZIP", "Éditeur de code",
        "Moniteur système", "Téléchargements", "Corbeille",
        "Gestionnaire de tâches", "Lecteur multimédia"
    };

    private final String[] icons = {
        "🖥️", "📁", "⌨️", "🧮",
        "📝", "⚙️", "🌐", "🖼️",
        "📅", "📦", "💻", "📊",
        "⬇️", "🗑️", "📈", "🎵"
    };

    private final List<View> openWindows = new ArrayList<>();

    private final Runnable clockUpdater = new Runnable() {
        @Override
        public void run() {
            if (clock != null) {
                clock.setText(new SimpleDateFormat(
                    "HH:mm", Locale.getDefault()
                ).format(new Date()));
            }
            handler.postDelayed(this, 1000);
        }
    };

    private final BroadcastReceiver batteryReceiver =
        new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateBattery(intent);
            }
        };

    private void fullscreen() {
        getWindow().setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        if (Build.VERSION.SDK_INT >= 28) {
            android.view.WindowManager.LayoutParams p =
                getWindow().getAttributes();

            p.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams
                    .LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

            getWindow().setAttributes(p);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) fullscreen();
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        fullscreen();
        createDesktop();

        IntentFilter filter =
            new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        Intent batteryIntent = registerReceiver(null, filter);
        if (batteryIntent != null) updateBattery(batteryIntent);

        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(
                batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            registerReceiver(batteryReceiver, filter);
        }

        receiverRegistered = true;
        handler.post(clockUpdater);
    }

    private int dp(float value) {
        return (int)(value * getResources()
            .getDisplayMetrics().density);
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(radius));
        return bg;
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private void createDesktop() {
        LinearLayout root = column();

        desktop = new FrameLayout(this);
        desktop.setBackground(new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[] {
                Color.rgb(9, 24, 48),
                Color.rgb(24, 57, 101),
                Color.rgb(34, 47, 83)
            }
        ));

        ScrollView scroll = new ScrollView(this);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setPadding(dp(12), dp(18), dp(12), dp(18));

        for (int i = 0; i < apps.length; i++) {
            final String app = apps[i];

            LinearLayout item = column();
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(3), dp(9), dp(3), dp(9));
            item.setBackground(rounded(
                Color.argb(25, 255, 255, 255), 13
            ));

            TextView icon = text(icons[i], 28, WHITE);
            icon.setGravity(Gravity.CENTER);

            TextView name = text(app, 10, WHITE);
            name.setGravity(Gravity.CENTER);
            name.setTypeface(null, Typeface.BOLD);

            item.addView(icon);
            item.addView(name);

            GridLayout.LayoutParams params =
                new GridLayout.LayoutParams();

            params.width = 0;
            params.height = dp(82);
            params.columnSpec =
                GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(dp(4), dp(4), dp(4), dp(4));

            grid.addView(item, params);
            item.setOnClickListener(v -> openApp(app));
        }

        scroll.addView(grid);
        desktop.addView(scroll);

        // TASKBAR NIICE OS
        LinearLayout taskbar = row();
        taskbar.setPadding(dp(8), 0, dp(8), 0);
        taskbar.setGravity(Gravity.CENTER_VERTICAL);
        taskbar.setBackgroundColor(Color.rgb(13, 21, 37));

        Button start = new Button(this);
        start.setText("⊞");
        start.setTextSize(25);
        start.setTextColor(WHITE);
        start.setBackgroundColor(Color.TRANSPARENT);
        start.setOnClickListener(v -> showStartMenu());
        taskbar.addView(start);

        Button files = new Button(this);
        files.setText("📁");
        files.setTextSize(18);
        files.setBackgroundColor(Color.TRANSPARENT);
        files.setOnClickListener(v -> openApp("Fichiers"));
        taskbar.addView(files);

        Button browser = new Button(this);
        browser.setText("🌐");
        browser.setTextSize(18);
        browser.setBackgroundColor(Color.TRANSPARENT);
        browser.setOnClickListener(v -> openApp("Navigateur"));
        taskbar.addView(browser);

        Button settings = new Button(this);
        settings.setText("⚙️");
        settings.setTextSize(18);
        settings.setBackgroundColor(Color.TRANSPARENT);
        settings.setOnClickListener(v -> openApp("Paramètres"));
        taskbar.addView(settings);

        View spacer = new View(this);
        taskbar.addView(spacer, new LinearLayout.LayoutParams(
            0, 1, 1
        ));

        clock = text("--:--", 13, WHITE);
        clock.setTypeface(null, Typeface.BOLD);
        taskbar.addView(clock);

        battery = text("  ▰  --%", 12, WHITE);
        battery.setPadding(dp(8), 0, dp(5), 0);
        taskbar.addView(battery);

        root.addView(desktop, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        root.addView(taskbar, new LinearLayout.LayoutParams(
            -1, dp(52)
        ));

        setContentView(root);
    }

    private void updateBattery(Intent intent) {
        if (intent == null || battery == null) return;

        int level = intent.getIntExtra(
            android.os.BatteryManager.EXTRA_LEVEL, -1
        );

        int scale = intent.getIntExtra(
            android.os.BatteryManager.EXTRA_SCALE, 100
        );

        int status = intent.getIntExtra(
            android.os.BatteryManager.EXTRA_STATUS, -1
        );

        if (level >= 0 && scale > 0) {
            int percent = level * 100 / scale;

            boolean charging =
                status == android.os.BatteryManager.BATTERY_STATUS_CHARGING
                || status == android.os.BatteryManager.BATTERY_STATUS_FULL;

            battery.setText(
                (charging ? "⚡ " : "▰ ") + percent + "%"
            );
        }
    }

    private void showStartMenu() {
        String[] entries = new String[apps.length + 1];
        System.arraycopy(apps, 0, entries, 0, apps.length);
        entries[apps.length] = "Éteindre NIICE OS";

        new AlertDialog.Builder(this)
            .setTitle("✦ NIICE OS")
            .setItems(entries, (d, which) -> {
                if (which == apps.length) {
                    finish();
                } else {
                    openApp(apps[which]);
                }
            })
            .show();
    }

    private void openApp(String name) {
        LinearLayout content = column();
        content.setPadding(dp(15), dp(12), dp(15), dp(15));
        content.setBackgroundColor(Color.rgb(242, 245, 250));

        switch (name) {
            case "Notes": notes(content); break;
            case "Calculatrice": calculator(content); break;
            case "Terminal": terminal(content); break;
            case "Paramètres": settings(content); break;
            case "Fichiers":
            case "Ce PC":
            case "Téléchargements": fileManager(content); break;
            case "Galerie": gallery(content); break;
            case "Navigateur": browser(content); break;
            case "Calendrier": calendar(content); break;
            case "Gestionnaire ZIP": zipManager(content); break;
            case "Éditeur de code": codeEditor(content); break;
            case "Moniteur système": systemMonitor(content); break;
            case "Corbeille": recycleBin(content); break;
            case "Gestionnaire de tâches": taskManager(content); break;
            case "Lecteur multimédia": mediaPlayer(content); break;
        }

        FrameLayout window = new FrameLayout(this);
        window.setBackground(rounded(WHITE, 16));
        window.setElevation(dp(15));

        LinearLayout panel = column();

        LinearLayout header = row();
        header.setPadding(dp(12), 0, dp(8), 0);
        header.setBackgroundColor(DARK);

        TextView title = text("✦  " + name, 14, WHITE);
        title.setTypeface(null, Typeface.BOLD);

        header.addView(title, new LinearLayout.LayoutParams(
            0, dp(43), 1
        ));

        Button minimize = new Button(this);
        minimize.setText("—");
        minimize.setTextColor(WHITE);
        minimize.setBackgroundColor(Color.TRANSPARENT);
        minimize.setOnClickListener(v -> window.setVisibility(View.GONE));
        header.addView(minimize);

        Button close = new Button(this);
        close.setText("✕");
        close.setTextColor(WHITE);
        close.setBackgroundColor(Color.TRANSPARENT);
        close.setOnClickListener(v -> {
            desktop.removeView(window);
            openWindows.remove(window);
        });
        header.addView(close);

        panel.addView(header);

        ScrollView body = new ScrollView(this);
        body.addView(content);
        panel.addView(body, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        window.addView(panel);

        int width = Math.min(
            getResources().getDisplayMetrics().widthPixels - dp(24),
            dp(540)
        );

        int height = (int)(
            getResources().getDisplayMetrics().heightPixels * 0.68
        );

        FrameLayout.LayoutParams params =
            new FrameLayout.LayoutParams(
                width, height, Gravity.CENTER
            );

        desktop.addView(window, params);
        window.bringToFront();
        openWindows.add(window);
    }

    private void notes(LinearLayout content) {
        android.content.SharedPreferences prefs =
            getSharedPreferences("niice_notes", MODE_PRIVATE);

        EditText edit = new EditText(this);
        edit.setGravity(Gravity.TOP);
        edit.setMinLines(9);
        edit.setHint("Écris tes notes...");
        edit.setText(prefs.getString("notes", ""));

        content.addView(edit);

        addButton(content, "💾 Enregistrer", () -> {
            prefs.edit().putString(
                "notes", edit.getText().toString()
            ).apply();
            toast("Notes enregistrées");
        });
    }

    private void calculator(LinearLayout content) {
        EditText a = numberInput("Premier nombre");
        EditText b = numberInput("Deuxième nombre");

        content.addView(a);
        content.addView(b);

        Spinner operator = new Spinner(this);
        String[] ops = {"+", "-", "×", "÷"};
        operator.setAdapter(new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_dropdown_item, ops
        ));
        content.addView(operator);

        TextView result = text("Résultat : ", 21, DARK);
        content.addView(result);

        addButton(content, "Calculer", () -> {
            try {
                double x = Double.parseDouble(a.getText().toString());
                double y = Double.parseDouble(b.getText().toString());
                double r;

                switch (operator.getSelectedItemPosition()) {
                    case 0: r = x + y; break;
                    case 1: r = x - y; break;
                    case 2: r = x * y; break;
                    default:
                        if (y == 0) throw new ArithmeticException();
                        r = x / y;
                }

                result.setText("Résultat : " + r);
            } catch (Exception e) {
                result.setText("Vérifie les nombres saisis.");
            }
        });
    }

    private EditText numberInput(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(
            android.text.InputType.TYPE_CLASS_NUMBER
            | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
        );
        return e;
    }

    private void terminal(LinearLayout content) {
        TextView output = text(
            "NIICE OS Terminal\nVersion 2.0\nTape help pour les commandes.\n",
            14, Color.rgb(0, 110, 60)
        );
        output.setTypeface(Typeface.MONOSPACE);
        content.addView(output);

        EditText command = new EditText(this);
        command.setHint("Commande...");
        command.setSingleLine(true);
        content.addView(command);

        addButton(content, "Exécuter", () -> {
            String cmd = command.getText().toString().trim();

            switch (cmd) {
                case "help":
                    output.setText(
                        "Commandes : help, date, about, device, battery, clear"
                    );
                    break;
                case "date":
                    output.setText(new Date().toString());
                    break;
                case "about":
                    output.setText("NIICE OS 2.0 - Desktop Android");
                    break;
                case "device":
                    output.setText(
                        android.os.Build.MANUFACTURER + " "
                        + android.os.Build.MODEL + "\nAndroid "
                        + android.os.Build.VERSION.RELEASE
                    );
                    break;
                case "battery":
                    output.setText(battery.getText());
                    break;
                case "clear":
                    output.setText("");
                    break;
                default:
                    output.setText("Commande inconnue : " + cmd);
            }
        });
    }

    private void settings(LinearLayout content) {
        content.addView(text("Paramètres NIICE OS", 21, DARK));

        addInfo(content, "Version", "NIICE OS 2.0");
        addInfo(content, "Appareil", android.os.Build.MODEL);
        addInfo(content, "Fabricant", android.os.Build.MANUFACTURER);
        addInfo(content, "Android", android.os.Build.VERSION.RELEASE);
        addInfo(content, "SDK", String.valueOf(
            android.os.Build.VERSION.SDK_INT
        ));

        addButton(content, "Informations Android", () -> {
            try {
                startActivity(new Intent(
                    android.provider.Settings.ACTION_SETTINGS
                ));
            } catch (Exception e) {
                toast("Paramètres Android indisponibles");
            }
        });

        addButton(content, "Personnalisation", () ->
            toast("Le choix des thèmes sera ajouté dans le module suivant.")
        );
    }

    private void fileManager(LinearLayout content) {
        content.addView(text("Explorateur de fichiers", 21, DARK));
        content.addView(text(
            "Sélectionne des fichiers dans le stockage autorisé du téléphone.",
            14, DARK
        ));

        addButton(content, "Parcourir les fichiers", () -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.setType("*/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(i, 101);
        });
    }

    private void gallery(LinearLayout content) {
        content.addView(text("Galerie", 21, DARK));

        addButton(content, "Choisir une image", () -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.setType("image/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(i, 102);
        });
    }

    private void browser(LinearLayout content) {
        content.addView(text("Navigateur", 21, DARK));

        EditText url = new EditText(this);
        url.setHint("https://example.com");
        url.setSingleLine(true);
        content.addView(url);

        addButton(content, "Ouvrir le site", () -> {
            String address = url.getText().toString().trim();

            if (!address.startsWith("http://")
                && !address.startsWith("https://")) {
                address = "https://" + address;
            }

            try {
                startActivity(new Intent(
                    Intent.ACTION_VIEW, Uri.parse(address)
                ));
            } catch (Exception e) {
                toast("Impossible d'ouvrir ce site");
            }
        });
    }

    private void calendar(LinearLayout content) {
        content.addView(text("Calendrier", 21, DARK));

        addButton(content, "Ouvrir le calendrier Android", () -> {
            try {
                startActivity(new Intent(
                    Intent.ACTION_MAIN
                ).addCategory(Intent.CATEGORY_APP_CALENDAR));
            } catch (Exception e) {
                toast("Aucun calendrier disponible");
            }
        });
    }

    private void zipManager(LinearLayout content) {
        content.addView(text("Gestionnaire ZIP", 21, DARK));

        addButton(content, "Sélectionner une archive ZIP", () -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.setType("application/zip");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(i, 103);
        });
    }

    private void codeEditor(LinearLayout content) {
        content.addView(text("Éditeur de code", 21, DARK));

        EditText code = new EditText(this);
        code.setGravity(Gravity.TOP);
        code.setMinLines(10);
        code.setTypeface(Typeface.MONOSPACE);
        code.setHint("// Écris ton code ici");
        content.addView(code);

        addButton(content, "Enregistrer le fichier", () -> {
            pendingCode = code.getText().toString();

            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TITLE, "mon-code.txt");
            startActivityForResult(i, 104);
        });
    }

    private void systemMonitor(LinearLayout content) {
        content.addView(text("Moniteur système", 21, DARK));

        Runtime r = Runtime.getRuntime();

        addInfo(content, "Appareil", android.os.Build.MODEL);
        addInfo(content, "Android", android.os.Build.VERSION.RELEASE);
        addInfo(content, "Processeurs visibles",
            String.valueOf(r.availableProcessors()));
        addInfo(content, "Mémoire libre de l'application",
            (r.freeMemory() / 1024 / 1024) + " Mo");
        addInfo(content, "Mémoire totale de l'application",
            (r.totalMemory() / 1024 / 1024) + " Mo");
        addInfo(content, "Batterie", battery.getText().toString());
    }

    private void recycleBin(LinearLayout content) {
        content.addView(text("Corbeille NIICE OS", 21, DARK));
        content.addView(text(
            "La corbeille interne sera connectée au gestionnaire de fichiers. "
            + "Aucun fichier du téléphone n'est supprimé par cette version.",
            14, DARK
        ));
    }

    private void taskManager(LinearLayout content) {
        content.addView(text("Gestionnaire de tâches", 21, DARK));

        TextView count = text(
            "Fenêtres ouvertes : " + openWindows.size(),
            16, DARK
        );
        content.addView(count);

        addButton(content, "Actualiser", () ->
            count.setText("Fenêtres ouvertes : " + openWindows.size())
        );

        addButton(content, "Fermer toutes les fenêtres", () -> {
            for (View v : new ArrayList<>(openWindows)) {
                desktop.removeView(v);
            }
            openWindows.clear();
            count.setText("Fenêtres ouvertes : 0");
        });
    }

    private void mediaPlayer(LinearLayout content) {
        content.addView(text("Lecteur multimédia", 21, DARK));

        addButton(content, "Choisir un fichier audio ou vidéo", () -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.setType("*/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(i, 105);
        });
    }

    private void addInfo(
        LinearLayout content, String title, String value) {
        TextView t = text(title + " : " + value, 14, DARK);
        t.setPadding(0, dp(8), 0, dp(8));
        content.addView(t);
    }

    private void addButton(
        LinearLayout content, String title, Runnable action) {
        Button b = new Button(this);
        b.setText(title);
        b.setOnClickListener(v -> action.run());
        content.addView(b);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        Uri uri = data.getData();
        if (uri == null) return;

        if (requestCode == 104) {
            try {
                java.io.OutputStream stream =
                    getContentResolver().openOutputStream(uri);

                if (stream != null) {
                    stream.write(pendingCode.getBytes("UTF-8"));
                    stream.close();
                    toast("Code enregistré !");
                }
            } catch (Exception e) {
                toast("Erreur pendant l'enregistrement");
            }
            return;
        }

        if (requestCode == 101 || requestCode == 102
            || requestCode == 103 || requestCode == 105) {
            String filename = uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(
                uri, null, null, null, null
            );

            if (cursor != null) {
                try {
                    int index = cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    );
                    if (cursor.moveToFirst() && index >= 0) {
                        filename = cursor.getString(index);
                    }
                } finally {
                    cursor.close();
                }
            }

            toast("Sélection : " + filename);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(clockUpdater);

        if (receiverRegistered) {
            unregisterReceiver(batteryReceiver);
        }

        super.onDestroy();
    }
}

