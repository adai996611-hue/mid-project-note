package com.example.android.notepad;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotesList extends AppCompatActivity {

    private static final String TAG = "NotesList";

    private static final int MENU_DELETE = Menu.FIRST;
    private static final int MENU_SHARE = Menu.FIRST + 1;
    private static final int MENU_INFO = Menu.FIRST + 2;

    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
            NotePad.Notes.COLUMN_NAME_NOTE
    };

    private static final int COLUMN_INDEX_TITLE = 1;

    // 定义 ListView 变量
    private ListView mListView;
    private SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. 加载布局
        setContentView(R.layout.noteslist_layout);

        // 2. 手动获取 ListView
        mListView = findViewById(android.R.id.list);

        // 设置空视图
        View emptyView = findViewById(android.R.id.empty);
        mListView.setEmptyView(emptyView);

        // 3. 注册上下文菜单
        registerForContextMenu(mListView);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }

        // 4. 执行查询
        Cursor cursor = managedQuery(
                getIntent().getData(),
                PROJECTION,
                null,
                null,
                NotePad.Notes.DEFAULT_SORT_ORDER
        );

        String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE };
        int[] viewIDs = { android.R.id.text1, R.id.text_date };

        mAdapter = new SimpleCursorAdapter(
                this,
                R.layout.noteslist_item,
                cursor,
                dataColumns,
                viewIDs
        );

        // 5. 设置 ViewBinder (时间格式化)
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.text_date) {
                    long time = cursor.getLong(columnIndex);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    ((TextView) view).setText(sdf.format(new Date(time)));
                    return true;
                }
                // 由于使用了 CardView 布局，这里不再需要斑马纹背景设置，保持代码整洁
                return false;
            }
        });

        // 6. 绑定 Adapter
        mListView.setAdapter(mAdapter);

        // 7. 设置点击监听 (使用显式 Intent 防止崩溃)
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
                String action = getIntent().getAction();
                if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
                    setResult(RESULT_OK, new Intent().setData(uri));
                } else {
                    Intent intent = new Intent(NotesList.this, NoteEditor.class);
                    intent.setAction(Intent.ACTION_EDIT);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        });

        // 8. 悬浮按钮逻辑
        FloatingActionButton fab = findViewById(R.id.fab_add);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(NotesList.this, NoteEditor.class);
                    intent.setAction(Intent.ACTION_INSERT);
                    intent.setData(getIntent().getData());
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        View actionView = searchItem.getActionView();

        if (actionView instanceof SearchView) {
            SearchView searchView = (SearchView) actionView;
            searchView.setQueryHint("搜索笔记...");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }

                @Override
                public boolean onQueryTextChange(String newText) {
                    String selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ?";
                    String[] selectionArgs = { "%" + newText + "%" };
                    Cursor newCursor = getContentResolver().query(
                            getIntent().getData(), PROJECTION, selection, selectionArgs, NotePad.Notes.DEFAULT_SORT_ORDER
                    );
                    mAdapter.changeCursor(newCursor);
                    return true;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_sort_desc) {
            updateCursor(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " DESC");
            return true;
        } else if (id == R.id.menu_sort_asc) {
            updateCursor(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " ASC");
            return true;
        } else if (id == R.id.menu_add) {
            Intent intent = new Intent(this, NoteEditor.class);
            intent.setAction(Intent.ACTION_INSERT);
            intent.setData(getIntent().getData());
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_paste) {
            // 【核心修复】改为调用本地粘贴方法，防止崩溃
            performPaste();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 处理粘贴逻辑
    private void performPaste() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            CharSequence text = item.getText();

            if (text != null) {
                Uri uri = getContentResolver().insert(getIntent().getData(), null);
                if (uri != null) {
                    ContentValues values = new ContentValues();
                    values.put(NotePad.Notes.COLUMN_NAME_NOTE, text.toString());
                    // 截取前10个字作为标题
                    String title = text.length() > 10 ? text.subSequence(0, 10).toString() : text.toString();
                    values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);

                    getContentResolver().update(uri, values, null, null);
                    Toast.makeText(this, "已从剪贴板创建笔记", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "剪贴板没有文本", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "剪贴板为空", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCursor(String sortOrder) {
        Cursor cursor = getContentResolver().query(
                getIntent().getData(), PROJECTION, null, null, sortOrder
        );
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }
        Cursor cursor = (Cursor) mAdapter.getItem(info.position);
        if (cursor == null) return;

        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));
        menu.add(0, MENU_DELETE, 0, "删除笔记");
        menu.add(0, MENU_SHARE, 1, "分享笔记");
        menu.add(0, MENU_INFO, 2, "查看详情");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        Cursor cursor = (Cursor) mAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case MENU_DELETE:
                showDeleteDialog(info.id);
                return true;
            case MENU_SHARE:
                String content = cursor.getString(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_NOTE));
                String title = cursor.getString(COLUMN_INDEX_TITLE);
                shareNote(title, content);
                return true;
            case MENU_INFO:
                String contentInfo = cursor.getString(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_NOTE));
                String titleInfo = cursor.getString(COLUMN_INDEX_TITLE);
                long timeInfo = cursor.getLong(cursor.getColumnIndexOrThrow(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));
                showNoteInfo(titleInfo, contentInfo, timeInfo);
                return true;
            default:
                return false;
        }
    }

    private void showDeleteDialog(final long noteId) {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定要删除这条笔记吗？")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), noteId);
                        getContentResolver().delete(noteUri, null, null);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void shareNote(String title, String content) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, "标题: " + title + "\n\n" + content);
        startActivity(Intent.createChooser(share, "分享笔记到..."));
    }

    private void showNoteInfo(String title, String content, long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateStr = sdf.format(new Date(time));
        int length = (content == null) ? 0 : content.length();
        String info = "标题：" + title + "\n字数：" + length + " 字\n修改时间：" + dateStr;

        new AlertDialog.Builder(this)
                .setTitle("笔记详情")
                .setMessage(info)
                .setPositiveButton("确定", null)
                .show();
    }
}