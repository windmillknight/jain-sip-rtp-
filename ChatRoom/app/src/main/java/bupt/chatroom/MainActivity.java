package bupt.chatroom;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import bupt.chatroom.Fragment.FriendFragment;
import bupt.chatroom.Fragment.MessageListFragment;
import bupt.chatroom.Fragment.MineFragment;

public class MainActivity extends AppCompatActivity {
    //当前fragment的名字
    private static final String CURRENT_FRAGMENT_NAME="STATE_FRAGMENT_SHOW";
    private int currentIndex = 0;

    private FragmentManager fragmentManager;
    private List<Fragment> fragments = new ArrayList<>();
    private Fragment currentFragment = new Fragment();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    currentIndex = 0;
                    showFragment();
                    return true;
                case R.id.navigation_dashboard:
                    currentIndex = 1;
                    showFragment();
                    return true;
                case R.id.navigation_notifications:
                    currentIndex = 2;
                    showFragment();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragmentManager = getSupportFragmentManager();

        // 内存重启时调用
        if(savedInstanceState!=null){
            currentIndex =savedInstanceState.getInt(CURRENT_FRAGMENT_NAME,0);

            fragments.removeAll(fragments);
            fragments.add(fragmentManager.findFragmentByTag("0"));
            fragments.add(fragmentManager.findFragmentByTag("1"));
            fragments.add(fragmentManager.findFragmentByTag("2"));

            restoreFragment();
        }else{
            fragments.add(new MessageListFragment());
            fragments.add(new FriendFragment());
            fragments.add(new MineFragment());

            showFragment();
        }
    }

    /**
     * 内存重启，保存当前fragment的名字
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putInt(CURRENT_FRAGMENT_NAME,currentIndex);
        super.onSaveInstanceState(outState);
    }

    private void showFragment(){
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        //如果之前没有添加过
        if(!fragments.get(currentIndex).isAdded()){
            transaction.hide(currentFragment).add(R.id.content,fragments.get(currentIndex),currentIndex+"");
        }else{
            transaction.hide(currentFragment).show(fragments.get(currentIndex));
        }
        currentFragment = fragments.get(currentIndex);
        transaction.commit();
    }

    /**
     * 恢复fragmen
     */
    private void restoreFragment(){
        FragmentTransaction mtransaction = fragmentManager.beginTransaction();
        for(int i = 0;i<fragments.size();i++){
            if(i==currentIndex){
                mtransaction.show(fragments.get(i));
            }else{
                mtransaction.hide(fragments.get(i));
            }
        }

        mtransaction.commit();

        currentFragment = fragments.get(currentIndex);
    }

}
