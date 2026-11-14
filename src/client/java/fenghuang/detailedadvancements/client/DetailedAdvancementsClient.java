package fenghuang.detailedadvancements.client;

import net.fabricmc.api.ClientModInitializer;

public class DetailedAdvancementsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		DetailedAdvancementsConfig.load();
	}
}