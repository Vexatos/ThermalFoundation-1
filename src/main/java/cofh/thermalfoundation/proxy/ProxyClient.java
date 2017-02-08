package cofh.thermalfoundation.proxy;

import cofh.api.core.IModelRegister;
import cofh.thermalfoundation.entity.monster.EntityBasalz;
import cofh.thermalfoundation.entity.monster.EntityBlitz;
import cofh.thermalfoundation.entity.monster.EntityBlizz;
import cofh.thermalfoundation.entity.projectile.EntityBasalzBolt;
import cofh.thermalfoundation.entity.projectile.EntityBlitzBolt;
import cofh.thermalfoundation.entity.projectile.EntityBlizzBolt;
import cofh.thermalfoundation.render.entity.RenderEntityAsIcon;
import cofh.thermalfoundation.render.entity.RenderEntityBasalz;
import cofh.thermalfoundation.render.entity.RenderEntityBlitz;
import cofh.thermalfoundation.render.entity.RenderEntityBlizz;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;

public class ProxyClient extends Proxy {

	/* INIT */
	@Override
	public void preInit(FMLPreInitializationEvent event) {

		super.preInit(event);

		for (int i = 0; i < modelList.size(); i++) {
			modelList.get(i).registerModels();
		}
		registerRenderInformation();
	}

	@Override
	public void initialize(FMLInitializationEvent event) {

		super.initialize(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {

		super.postInit(event);
	}

	/* REGISTRATION */
	public void registerRenderInformation() {

		RenderingRegistry.registerEntityRenderingHandler(EntityBasalz.class, new IRenderFactory<EntityBasalz>() {
			@Override
			public Render<? super EntityBasalz> createRenderFor(RenderManager manager) {

				return new RenderEntityBasalz(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityBlitz.class, new IRenderFactory<EntityBlitz>() {
			@Override
			public Render<? super EntityBlitz> createRenderFor(RenderManager manager) {

				return new RenderEntityBlitz(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityBlizz.class, new IRenderFactory<EntityBlizz>() {
			@Override
			public Render<? super EntityBlizz> createRenderFor(RenderManager manager) {

				return new RenderEntityBlizz(manager);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityBlizzBolt.class, new IRenderFactory<EntityBlizzBolt>() {
			@Override
			public Render<? super EntityBlizzBolt> createRenderFor(RenderManager manager) {

				return new RenderEntityAsIcon(manager).setIcon("thermalfoundation:items/material/dust_blizz");
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityBlitzBolt.class, new IRenderFactory<EntityBlitzBolt>() {
			@Override
			public Render<? super EntityBlitzBolt> createRenderFor(RenderManager manager) {

				return new RenderEntityAsIcon(manager).setIcon("thermalfoundation:items/material/dust_blitz");
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityBasalzBolt.class, new IRenderFactory<EntityBasalzBolt>() {
			@Override
			public Render<? super EntityBasalzBolt> createRenderFor(RenderManager manager) {

				return new RenderEntityAsIcon(manager).setIcon("thermalfoundation:items/material/dust_basalz");
			}
		});
	}

	/* HELPERS */
	public boolean addIModelRegister(IModelRegister modelRegister) {

		return modelList.add(modelRegister);
	}

	private static ArrayList<IModelRegister> modelList = new ArrayList<IModelRegister>();

}