package fenghuang.detailedadvancements.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class DetailedAdvancementsModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return DetailedAdvancementsConfigScreen::new;
    }
}
