package com.niice.os;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private FrameLayout desktop;
    private LinearLayout taskbar;

    private int blue = Color.rgb(35, 112, 210);
    private int dark = Color.rgb(24, 35, 55);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(20, 35, 60));
        getWindow().setNavigationBarColor(Color.BLACK);
        createDesktop();
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(radius);
        return bg;
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private void createDesktop() {
        desktop = new FrameLayout(this);
        desktop.setBackgroundColor(Color.rgb(24, 48, 83));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);

        FrameLayout area = new FrameLayout(this);
        area.setBackgroundColor(Color.rgb(29, 57, 96));

        LinearLayout icons = new LinearLayout(this);
        icons.setOrientation(LinearLayout.VERTICAL);
        icons.setPadding(12, 15, 12, 10);

        String[] names = {
            "Ce PC", "Fichiers", "Calculatrice",
            "Notes", "Paramètres", "Terminal"
        };

        String[] symbols = {
            "🖥️", "📁", "🧮", "📝", "⚙️", "⌨️"
        };

        for (int i = 0; i < names.length; i++) {
            final String name = names[i];

            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(5, 5, 5, 10);

            TextView icon = text(symbols[i], 27, Color.WHITE);
            TextView label = text(name, 11, Color.WHITE);

            item.addView(icon);
            item.addView(label);

            item.setOnClickListener(v -> openWindow(name));

            icons.addView(item);
        }

        area.addView(icons);

        main.addView(area, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        taskbar = new LinearLayout(this);
        taskbar.setGravity(Gravity.CENTER);
        taskbar.setOrientation(LinearLayout.HORIZONTAL);
        taskbar.setPadding(8, 8, 8, 8);
        taskbar.setBackground(shape(Color.rgb(18, 27, 43), 0));

        Button start = new Button(this);
        start.setText("⊞");
        start.setTextSize(23);
        start.setTextColor(Color.WHITE);
        start.setBackgroundColor(Color.TRANSPARENT);
        start.setOnClickListener(v -> showStartMenu());

        TextView clock = text("NIICE OS  |  3:21", 13, Color.WHITE);

        taskbar.addView(start);
        taskbar.addView(clock);

        main.addView(taskbar, new LinearLayout.LayoutParams(
            -1, 58
        ));

        desktop.addView(main);
        setContentView(desktop);
    }

    private void showStartMenu() {
        String[] apps = {
            "Ce PC", "Fichiers", "Calculatrice",
            "Notes", "Paramètres", "Terminal"
        };

        new AlertDialog.Builder(this)
            .setTitle("Démarrer - NIICE OS")
            .setItems(apps, (dialog, which) -> openWindow(apps[which]))
            .setNegativeButton("Fermer", null)
            .show();
    }

    private void openWindow(String name) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(20, 15, 20, 15);
        content.setBackgroundColor(Color.WHITE);

        TextView title = text(name, 22, dark);
        title.setTypeface(null, Typeface.BOLD);

        content.addView(title);

        TextView description = text(
            "Bienvenue dans " + name + " de NIICE OS.",
            15,
            Color.DKGRAY
        );

        content.addView(description);

        if (name.equals("Terminal")) {
            TextView terminal = text(
                "NIICE OS Terminal\n\nVersion 1.0\nTerminal en développement...",
                14,
                Color.GREEN
            );
            terminal.setBackgroundColor(Color.BLACK);
            content.addView(terminal);
        }

        if (name.equals("Calculatrice")) {
            Button calculate = new Button(this);
            calculate.setText("Calculatrice bientôt disponible");
            content.addView(calculate);
        }

        new AlertDialog.Builder(this)
            .setTitle("NIICE OS — " + name)
            .setView(content)
            .setPositiveButton("Fermer", null)
            .show();
    }
}
