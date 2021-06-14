package com.revealprecision.reveal.shadow;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import com.revealprecision.reveal.view.DrawerMenuView;

/**
 * Created by samuelgithengi on 1/27/20.
 */
@Implements(DrawerMenuView.class)
public class DrawerMenuViewShadow {

    @Implementation
    public void initializeDrawerLayout() {//Do nothing
    }

    @Implementation
    public void onResume() {//Do nothing
    }

}
