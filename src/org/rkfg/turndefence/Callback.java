package org.rkfg.turndefence;

public interface Callback {
    /**
     * Called on menu hiding for any reason.
     */
    public void cancel();

    /**
     * Called when user selects an item menu.
     * @param param contains building parameters.
     */
    public void done(BuildingParams param);
}
