package com.example.android.notepad;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NotePad {
    // 这里的 AUTHORITY 必须和 AndroidManifest.xml 里的 android:authorities 一模一样！
    public static final String AUTHORITY = "com.google.provider.NotePad";

    // 构造方法私有，防止实例化
    private NotePad() {
    }

    public static final class Notes implements BaseColumns {
        // 私有构造方法
        private Notes() {
        }

        // 定义表名
        public static final String TABLE_NAME = "notes";

        // 定义 Scheme
        private static final String SCHEME = "content://";

        // 路径
        private static final String PATH_NOTES = "/notes";
        private static final String PATH_NOTE_ID = "/notes/";
        private static final String PATH_LIVE_FOLDER = "/live_folders/notes"; // 新增路径

        // 这里的 ID 列索引
        public static final int NOTE_ID_PATH_POSITION = 1;
        public static final int LIVE_FOLDER_NOTE_ID_PATH_POSITION = 2;

        // 完整的 URI 地址
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "#");

        // 【修复点】补上缺失的 LIVE_FOLDER_URI
        public static final Uri LIVE_FOLDER_URI = Uri.parse(SCHEME + AUTHORITY + PATH_LIVE_FOLDER);

        // MIME 类型定义
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";

        // 默认排序：按修改时间降序
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        // --- 数据库列名定义 ---
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_NOTE = "note";
        public static final String COLUMN_NAME_CREATE_DATE = "created";
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";
    }
}