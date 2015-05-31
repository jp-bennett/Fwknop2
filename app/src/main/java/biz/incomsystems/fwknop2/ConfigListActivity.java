package biz.incomsystems.fwknop2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

/**
 * An activity representing a list of. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ConfigDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ConfigListFragment} and the item details
 * (if present) is a {@link ConfigDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ConfigListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ConfigListActivity extends FragmentActivity
        implements ConfigListFragment.Callbacks {
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_config_list);



        if (findViewById(R.id.config_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ConfigListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.config_list))
                    .setActivateOnItemClick(true);
        }


    }
    /**
     * Callback method from {@link ConfigListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String nick) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ConfigDetailFragment.ARG_ITEM_ID, nick);
            ConfigDetailFragment fragment = new ConfigDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.config_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ConfigDetailActivity.class);
            detailIntent.putExtra(ConfigDetailFragment.ARG_ITEM_ID, nick);
            startActivity(detailIntent);
        }
    }

    protected void onNewIntent(Intent intent) {
        super. onNewIntent(intent);
        setIntent(intent);
        ConfigListFragment listFrag = new ConfigListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.config_list, listFrag);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

    }

}

