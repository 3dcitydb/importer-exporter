package de.tub.citydb.modules.kml.database;

import java.io.File;
import java.sql.Connection;

import de.tub.citydb.api.database.BalloonTemplateFactory;
import de.tub.citydb.api.database.BalloonTemplateHandler;

public class BalloonTemplateFactoryImpl implements BalloonTemplateFactory {
	private static BalloonTemplateFactoryImpl instance;
	
	private BalloonTemplateFactoryImpl() {
		// just to thwart instantiation
	}
	
	public static synchronized BalloonTemplateFactory getInstance() {
		if (instance == null)
			instance = new BalloonTemplateFactoryImpl();
		
		return instance;
	}
	
	@Override
	public BalloonTemplateHandler createNewBalloonTemplateHandler(File templateFile, Connection connection) {
		return new BalloonTemplateHandlerImpl(templateFile, connection);
	}

	@Override
	public BalloonTemplateHandler createNewBalloonTemplateHandler(String templateString, Connection connection) {
		return new BalloonTemplateHandlerImpl(templateString, connection);
	}

}
