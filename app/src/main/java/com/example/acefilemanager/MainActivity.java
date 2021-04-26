package com.example.acefilemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout1);



    }

    class TextAdapter extends BaseAdapter {

        private List<String> data = new ArrayList<>();

        private boolean[] selection;

        public void setData(List<String> data) {
            if (data != null) {
                this.data.clear();
                if (data.size() > 0) {
                    this.data.addAll(data);
                }
                notifyDataSetChanged();
            }
        }

        public void setSelection(boolean[] selection) {
            if(selection!=null) {
                this.selection = new boolean[selection.length];
                for(int i=0;i<selection.length;i++){
                    this.selection[i]=selection[i];
                }
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.textItem)));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            final String path = getItem(position);
            holder.info.setText(path);
            if(selection!= null) {
                if(selection[position]) {
                    holder.info.setBackgroundColor(Color.YELLOW);
                }else{
                    holder.info.setBackgroundColor(Color.WHITE);
                }
            }

            return convertView;
        }

        class ViewHolder {
            TextView info;

            ViewHolder(TextView info) {
                this.info = info;
            }
        }
    }

    private static final int REQUEST_PERMISSIONS = 1234;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSIONS_COUNT = 2;

    @SuppressLint("NewApi")
    private boolean isPermissionsDenied() {
        int i = 0;
        while (i < PERMISSIONS_COUNT) {
            if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            i++;
        }
        return false;

    }

    private boolean isManagerInitialized = false;

    private boolean[] selection;

    private int filesFoundCount;

    private File[] files;

    private List<String> filesList;

    private File dir;

    private String copyPath;

    private int selectedItemIndex;

    private String currentPath;

    private Button updateButton;



    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isPermissionsDenied()) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }
        if (!isManagerInitialized) {
            currentPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            final String rootPath = currentPath.substring(0, currentPath.lastIndexOf('/'));
            final TextView outputPath = findViewById(R.id.outputPath);
            final ListView listView = findViewById(R.id.listView);
            final TextAdapter textAdapter1 = new TextAdapter();
            listView.setAdapter(textAdapter1);
            filesList = new ArrayList<>();



           updateButton = findViewById(R.id.update);
            updateButton.setOnClickListener((new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    outputPath.setText(currentPath.substring(currentPath.lastIndexOf( '/')+1));
                    dir = new File(currentPath);
                    files = dir.listFiles();
                    filesFoundCount = files.length;
                    selection = new boolean[filesFoundCount];
                    textAdapter1.setSelection(selection);
                    filesList.clear();


                    for (int i = 0; i < filesFoundCount; i++) {
                        filesList.add(String.valueOf(files[i].getName()));
                    }
                    textAdapter1.setData(filesList);

                }
            }));

            updateButton.callOnClick();


            final ImageButton goBackButton = findViewById(R.id.goBack);
            goBackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentPath.equals(rootPath)) {
                        return;
                    }
                    currentPath = currentPath.substring(0, currentPath.lastIndexOf('/'));
                    updateButton.callOnClick();
                    selection = new boolean[files.length];
                    textAdapter1.setSelection(selection);
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    currentPath = files[position].getAbsolutePath();
                    updateButton.callOnClick();
                }
            });


            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    selection[position] = !selection[position];
                    textAdapter1.setSelection(selection);
                    int selectionCount = 0;
                    for (boolean aSelection : selection) {
                        if (aSelection) {
                            selectionCount++;
                        }
                    }
                    if (selectionCount>0) {
                        if(selectionCount==1) {
                            selectedItemIndex = position;
                            findViewById(R.id.rename).setVisibility(View.VISIBLE);
                        }else {
                            findViewById(R.id.rename).setVisibility(View.GONE);
                        }
                        findViewById(R.id.Bar).setVisibility(view.VISIBLE);
                    } else {
                        findViewById(R.id.Bar).setVisibility(view.GONE);
                    }
                    return true;
                }



            });


            final ImageButton button1 = findViewById(R.id.button1);
            final ImageButton button2 = findViewById(R.id.rename);
            final ImageButton button3 = findViewById(R.id.copy);

            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MainActivity.this);
                    deleteDialog.setTitle("Delete");
                    deleteDialog.setMessage("Confirm delete?");
                    deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for(int i = 0; i<files.length; i++) {
                                if(selection[i]) {
                                    deleteItem(files[i]);
                                    selection[i]=false;
                                }
                            }
                            updateButton.callOnClick();
                        }
                    });
                    deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    deleteDialog.show();
                }
            });

            final ImageButton createNewFolder = findViewById(R.id.newFolder);

            createNewFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder newFolderDialog = new AlertDialog.Builder( MainActivity.this);
                    newFolderDialog.setTitle("New Folder");
                    final EditText input = new EditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    newFolderDialog.setView(input);
                    newFolderDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final File newFolder = new File(currentPath+"/"+input.getText());
                            if(!newFolder.exists()){
                                newFolder.mkdir();
                                updateButton.callOnClick();
                            }
                        }
                    });
                    newFolderDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            updateButton.callOnClick();
                        }
                    });
                    newFolderDialog.show();
                }
            });

            final ImageButton renameButton = findViewById(R.id.rename);
            renameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder renameDialog = new AlertDialog.Builder(MainActivity.this);
                    renameDialog.setTitle("Enter name:");
                    final EditText input = new EditText(MainActivity.this);
                    final String renamePath = files[selectedItemIndex].getAbsolutePath();
                    input.setText(renamePath.substring(renamePath.lastIndexOf('/')));
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    renameDialog.setView(input);
                    renameDialog.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String s = new File(renamePath).getParent() + "/"+ input.getText();
                            File newFile = new File(s);
                            new File(renamePath).renameTo(newFile);
                            updateButton.callOnClick();
                            selection = new boolean[files.length];
                            textAdapter1.setSelection(selection);
                        }
                    });
                    renameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            updateButton.callOnClick();
                        }
                    });
                    renameDialog.show();
                }
            });

            final ImageButton copyButton = findViewById(R.id.copy);
            copyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copyPath = files[selectedItemIndex].getAbsolutePath();
                    selection = new boolean[files.length];
                    textAdapter1.setSelection(selection);
                    findViewById(R.id.paste).setVisibility(View.VISIBLE);
                }
            });

            final ImageButton pasteButton = findViewById(R.id.paste);
            pasteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pasteButton.setVisibility(View.GONE);
                    String dstPath = currentPath + copyPath.substring(copyPath.lastIndexOf('/'));
                    copy(new File(copyPath), new File(dstPath));
                    updateButton.callOnClick();
                }
            });


            isManagerInitialized = true;
        }else {
            updateButton.callOnClick();
        }
    }

    private void copy(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            byte[] buf = new byte[1024];
            int len;
            while((len= in.read(buf))>0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteItem(File isFolderOrFile) {
        if(isFolderOrFile.isDirectory()) {
            if(isFolderOrFile.list().length == 0) {
                isFolderOrFile.delete();
            } else {
                String files[] = isFolderOrFile.list();
                for (String temp : files) {
                    File fileToDelete = new File(isFolderOrFile, temp);
                    deleteItem(fileToDelete);
                }
                if(isFolderOrFile.list().length==0) {
                    isFolderOrFile.delete();
                }
            }
        }else{
            isFolderOrFile.delete();
        }

    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS && grantResults.length > 0) {
            if (isPermissionsDenied()) {
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            } else {
                onResume();
            }
        }
    }
}