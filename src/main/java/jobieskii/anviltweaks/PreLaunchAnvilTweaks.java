package jobieskii.anviltweaks;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
public class PreLaunchAnvilTweaks implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        MixinExtrasBootstrap.init();
    }
}
