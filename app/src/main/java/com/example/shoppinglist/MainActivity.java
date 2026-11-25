
package com.example.shoppinglist;

import android.os.Bundle;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.*;
import android.database.Cursor;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Работа с базой данных
    private DBHelper dbHelper;

    // Элементы интерфейса
    private ListView listView;

    private TextView tvCount;
    private TextView tvTotal;

    private EditText etName;
    private EditText etPrice;

    private EditText etMin;
    private EditText etMax;

    private Spinner spCategory;
    private Spinner spSort;

    private LinearLayout formLayout;

    private Button btnShowForm;
    private Button btnSave;
    private Button btnSort;
    private Button btnFilter;
    private Button btnReset;

    // Адаптер для отображения данных
    private SimpleCursorAdapter adapter;

    // Параметры сортировки
    private boolean ascending = true;

    // Текущий режим отображения
    private boolean filterEnabled = false;

    private double minFilter;
    private double maxFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Создание подключения к базе SQLite
        dbHelper = new DBHelper(this);

        // Инициализация элементов
        initializeViews();

        // Настройка выпадающих списков
        setupSpinners();

        // Настройка адаптера
        setupAdapter();

        // Обработчики кнопок
        setupButtons();

        // Загрузка покупок
        loadPurchases();

        // Обновление статистики
        updateStatistics();
        setupDelete();
    }

    // Инициализация элементов интерфейса
    private void initializeViews() {

        listView = findViewById(R.id.listView);

        tvCount = findViewById(R.id.tvCount);
        tvTotal = findViewById(R.id.tvTotal);

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);

        etMin = findViewById(R.id.etMin);
        etMax = findViewById(R.id.etMax);

        spCategory = findViewById(R.id.spCategory);
        spSort = findViewById(R.id.spSort);

        formLayout = findViewById(R.id.formLayout);

        btnShowForm = findViewById(R.id.btnShowForm);
        btnSave = findViewById(R.id.btnSave);
        btnSort = findViewById(R.id.btnSort);
        btnFilter = findViewById(R.id.btnFilter);
        btnReset = findViewById(R.id.btnReset);
    }

    // Настройка выпадающих списков
    private void setupSpinners() {

        String[] categories = {
                "Продукты",
                "Техника",
                "Одежда",
                "Услуги",
                "Подарки",
                "Другое"
        };

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                );

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCategory.setAdapter(categoryAdapter);

        String[] sortOptions = {
                "По порядку добавления",
                "Название",
                "Стоимость"
        };

        ArrayAdapter<String> sortAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        sortOptions
                );

        sortAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spSort.setAdapter(sortAdapter);
    }

    // Создание SimpleCursorAdapter
    private void setupAdapter() {

        String[] from = {
                DBHelper.COLUMN_NAME,
                DBHelper.COLUMN_CATEGORY,
                DBHelper.COLUMN_DATE,
                DBHelper.COLUMN_PRICE
        };

        int[] to = {
                R.id.tvPurchaseName,
                R.id.tvPurchaseCategory,
                R.id.tvPurchaseDate,
                R.id.tvPurchasePrice
        };

        Cursor cursor = dbHelper.getAllPurchases(
                "По порядку добавления"
        );

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.item_purchase,
                cursor,
                from,
                to,
                0
        );

        // Форматирование стоимости
        adapter.setViewBinder((view, cursor1, columnIndex) -> {

            if (view.getId() == R.id.tvPurchasePrice) {

                double price = cursor1.getDouble(columnIndex);

                ((TextView) view).setText(
                        String.format(
                                Locale.getDefault(),
                                "%.2f руб.",
                                price
                        )
                );

                return true;
            }

            return false;
        });

        listView.setAdapter(adapter);
    }

    // Настройка обработчиков кнопок
    private void setupButtons() {

        // Показать или скрыть форму
        btnShowForm.setOnClickListener(v -> {

            if (formLayout.getVisibility() ==
                    LinearLayout.GONE) {

                formLayout.setVisibility(LinearLayout.VISIBLE);

                btnShowForm.setText("Скрыть форму");

            } else {

                formLayout.setVisibility(LinearLayout.GONE);

                btnShowForm.setText("Добавить покупку");
            }
        });

        // Добавление записи
        btnSave.setOnClickListener(v -> addPurchase());

        // Сортировка
        btnSort.setOnClickListener(v -> sortPurchases());

        // Фильтрация
        btnFilter.setOnClickListener(v -> applyFilter());

        // Сброс фильтра
        btnReset.setOnClickListener(v -> resetFilter());
    }

    // Добавление новой покупки
    private void addPurchase() {

        String name = etName.getText().toString().trim();

        String priceText = etPrice.getText()
                .toString()
                .trim()
                .replace(',', '.');

        // Проверка названия
        if (name.isEmpty()) {

            etName.setError("Введите название товара");
            return;
        }

        // Проверка стоимости
        if (priceText.isEmpty()) {

            etPrice.setError("Введите стоимость");
            return;
        }

        double price;

        try {

            price = Double.parseDouble(priceText);

        } catch (NumberFormatException e) {

            etPrice.setError("Некорректная стоимость");
            return;
        }

        if (!Double.isFinite(price) || price < 0) {

            etPrice.setError("Стоимость должна быть неотрицательной");
            return;
        }

        // Получение категории
        String category = spCategory.getSelectedItem().toString();

        // Получение текущей даты
        String date = new SimpleDateFormat(
                "dd.MM.yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date());

        // Сохранение записи в SQLite
        boolean result = dbHelper.addPurchase(
                name,
                price,
                category,
                date
        );

        if (result) {

            Toast.makeText(
                    this,
                    "Покупка успешно добавлена",
                    Toast.LENGTH_SHORT
            ).show();

            // Очистка полей
            etName.setText("");
            etPrice.setText("");

            // Скрытие формы
            formLayout.setVisibility(LinearLayout.GONE);

            btnShowForm.setText("Добавить покупку");

            // Сброс фильтра, чтобы увидеть новую запись
            filterEnabled = false;

            etMin.setText("");
            etMax.setText("");

            // Обновление списка и статистики
            loadPurchases();
            updateStatistics();

        } else {

            Toast.makeText(
                    this,
                    "Ошибка сохранения покупки",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Загрузка покупок из базы данных
    private void loadPurchases() {

        Cursor cursor;

        if (filterEnabled) {

            cursor = dbHelper.getPurchasesByPrice(
                    minFilter,
                    maxFilter
            );

        } else {

            cursor = dbHelper.getSortedPurchases(
                    spSort.getSelectedItem().toString(),
                    ascending
            );
        }

        // Передача нового курсора адаптеру
        adapter.changeCursor(cursor);
    }

    // Обновление общей статистики
    private void updateStatistics() {

        int count = dbHelper.getPurchaseCount();

        double total = dbHelper.getTotalPrice();

        tvCount.setText(
                "Количество покупок: " + count
        );

        tvTotal.setText(
                String.format(
                        Locale.getDefault(),
                        "Общая стоимость: %.2f руб.",
                        total
                )
        );
    }

    // Сортировка записей
    private void sortPurchases() {

        // Переключение направления сортировки
        ascending = !ascending;

        if (ascending) {

            btnSort.setText(
                    "Сортировать по возрастанию"
            );

        } else {

            btnSort.setText(
                    "Сортировать по убыванию"
            );
        }

        filterEnabled = false;

        loadPurchases();
    }

    // Фильтрация по диапазону стоимости
    private void applyFilter() {

        String minText = etMin.getText()
                .toString()
                .trim()
                .replace(',', '.');

        String maxText = etMax.getText()
                .toString()
                .trim()
                .replace(',', '.');

        if (minText.isEmpty() || maxText.isEmpty()) {

            Toast.makeText(
                    this,
                    "Введите обе границы диапазона",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            minFilter = Double.parseDouble(minText);

            maxFilter = Double.parseDouble(maxText);

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Введите корректные значения",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!Double.isFinite(minFilter) ||
                !Double.isFinite(maxFilter) ||
                minFilter < 0 ||
                maxFilter < minFilter) {

            Toast.makeText(
                    this,
                    "Проверьте границы диапазона",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        filterEnabled = true;

        loadPurchases();

        Toast.makeText(
                this,
                "Фильтр применён",
                Toast.LENGTH_SHORT
        ).show();
    }
    // Удаление покупки при долгом нажатии
    private void setupDelete() {

        listView.setOnItemLongClickListener((parent, view, position, id) -> {

            Cursor cursor = (Cursor) adapter.getItem(position);

            if (cursor == null) {
                return true;
            }

            long purchaseId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID)
            );

            String purchaseName = cursor.getString(
                    cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME)
            );

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Удаление покупки")
                    .setMessage("Вы действительно хотите удалить «"
                            + purchaseName + "»?")
                    .setPositiveButton("Удалить", (dialog, which) -> {

                        boolean deleted = dbHelper.deletePurchase(purchaseId);

                        if (deleted) {
                            loadPurchases();
                            updateStatistics();

                            Toast.makeText(
                                    MainActivity.this,
                                    "Покупка удалена",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Не удалось удалить покупку",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();

            return true;
        });
    }
    // Сброс фильтра
    private void resetFilter() {

        filterEnabled = false;

        etMin.setText("");
        etMax.setText("");

        loadPurchases();

        Toast.makeText(
                this,
                "Фильтр сброшен",
                Toast.LENGTH_SHORT
        ).show();
    }

    // Закрытие курсора и базы данных
    @Override
    protected void onDestroy() {

        if (adapter != null) {
            adapter.changeCursor(null);
        }

        if (dbHelper != null) {
            dbHelper.close();
        }

        super.onDestroy();
    }
}
