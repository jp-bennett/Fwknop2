/*
This file is part of Fwknop2.

    Fwknop2 is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */
package biz.incomsystems.fwknop2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import java.util.List;

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
    ConfigDetailFragment fragment;
    public boolean mTwoPane; // Whether in two-pane mode.

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
        SharedPreferences prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        boolean haveWeShownPreferences = prefs.getBoolean("HaveShownPrefs", false);

        if (!haveWeShownPreferences) {
            Intent detailIntent = new Intent(this, HelpActivity.class);
            startActivity(detailIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments())
        // this overcomes what may be a bug in the android framework. Pushes the result into fragment so it can get to the nested class.
        {
            if (fragment != null)
            {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }

        // This is important if you want to be able to interact with JuiceSSH sessions that you
        // have started otherwise the plugin won't have access.
        //if(requestCode == 2585){
        //    client.gotActivityResult(requestCode, resultCode, data);
       // }
    }

    public void onItemSaved() {
        List<Fragment> allFragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : allFragments) {
            if (fragment instanceof ConfigListFragment) {
                ((ConfigListFragment) fragment).onUpdate();
            }
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
            fragment = new ConfigDetailFragment();
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
    //public void onCheckboxClicked(View view) {
    //    fragment.onCheckboxClicked(view);
    //}

    protected void onNewIntent(Intent intent) {
        super. onNewIntent(intent);
        setIntent(intent);
        ConfigListFragment listFrag = new ConfigListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.config_list, listFrag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}

