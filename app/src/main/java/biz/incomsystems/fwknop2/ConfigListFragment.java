package biz.incomsystems.fwknop2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;



/**
 * A list fragment representing a list of . This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ConfigDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ConfigListFragment extends ListFragment {
    public ArrayAdapter customAdapter;
    ArrayList array_list = new ArrayList();
    DBHelper mydb;
    private String output;

    //These are the configs to pass to the native code
    public native String sendSPAPacket();
    public String access_str;
    public String allowip_str;
    public String tcpAccessPorts_str;
    public String passwd_str;
    public String hmac_str;
    public String destip_str;
    public String destport_str;
    public String fw_timeout_str;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
       // void onUpdate();
        void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }

    };

    protected void onNewIntent(Intent intent) {
        //super. onNewIntent(intent);
        //setIntent(intent);
        mydb = new DBHelper(getActivity());
        array_list = mydb.getAllConfigs();
        customAdapter = (ArrayAdapter) getListAdapter();
        customAdapter.notifyDataSetChanged();

    }

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConfigListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mydb = new DBHelper(getActivity());
        array_list = mydb.getAllConfigs();
        customAdapter = new ArrayAdapter<ArrayList>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                array_list);
        setListAdapter(customAdapter);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        registerForContextMenu(this.getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.listmenu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        customAdapter = (ArrayAdapter) getListAdapter();

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {

            case R.id.delete: // Deleting the selected option
                mydb.deleteConfig(((TextView) info.targetView).getText().toString());
                array_list.remove(info.position);
                customAdapter.notifyDataSetChanged();
                return true;

            case R.id.knock:
                send(((TextView) info.targetView).getText().toString());

            default:
                return super.onContextItemSelected(item);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
       // if (!(activity instanceof Callbacks)) {
       //     throw new IllegalStateException("Activity must implement fragment's callbacks.");
       // }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(this.getListAdapter().getItem(position).toString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    public void sendSPA() {
        startSPASend();
    }

    //    Start calling the JNI interface
    public synchronized void startSPASend() {
        output = sendSPAPacket();
        //sendHandlerMessage(handler, 1003);
        //  if (startApp) {
        //    startApp();
        // }
    }

    public int send(String nick){
        loadNativeLib("libfwknop.so", "/data/data/biz.incomsystems.fwknop2/lib");
        //mydb = new DBHelper(Fwknop2.getContext());
        Cursor CurrentIndex = mydb.getData(nick);
        CurrentIndex.moveToFirst();
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
       // SharedPreferences.Editor edit = prefs.edit();
        tcpAccessPorts_str = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_TCP_PORTS));
       // edit.putString("access_str", ",tcp/" + CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_TCP_PORTS)));
        access_str="tcp/" + CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_TCP_PORTS));
        allowip_str = "0.0.0.0";
        passwd_str = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_KEY));
        hmac_str = "";
        destip_str = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_IP));
        destport_str = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_PORT));
        fw_timeout_str = CurrentIndex.getString(CurrentIndex.getColumnIndex(DBHelper.CONFIGS_COLUMN_SERVER_TIMEOUT));
       // edit.commit();
        this.sendSPA();
        return 0;
    }

    private void loadNativeLib(String lib, String destDir) {
        if (true) {
            String libLocation = destDir + "/" + lib;
            try {
                System.load(libLocation);
            } catch (Exception ex) {
                Log.e("JNIExample", "failed to load native library: " + ex);
            }
        }

    }

    public Handler handler = new Handler() {

        @Override
        public synchronized void handleMessage(Message msg) {
            Bundle b = msg.getData();
            Integer messageType = (Integer) b.get("message_type");

            Toast.makeText(getActivity(), messageType.toString(), Toast.LENGTH_LONG).show();
//            if (messageType != null && messageType == IPS_RESOLVED) {
//                progDialog.dismiss();
//            } else if (messageType != null && messageType == EXTIP_NOTRESOLVED) {
//                progDialog.dismiss();
//                UIAlert("Error", "Could not resolve your external IP. This means that "
//                        + "you're not connected to the internet or ifconfig.me "
//                        + "is not be accesible right now", activity);
//            } else if (messageType != null && messageType == LOCALIP_NOTRESOLVED) {
//                progDialog.dismiss();
//                UIAlert("Error", "Could not find any IP, makes sure you have an internet connection", activity);
//            } else if (messageType != null && messageType == SPA_SENT) {
//                Toast.makeText(activity, output, Toast.LENGTH_LONG).show();
//            }

        }
    };

}
