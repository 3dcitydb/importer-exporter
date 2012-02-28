package de.tub.citydb.api.database;

import java.io.File;
import java.sql.Connection;

public interface BalloonTemplateFactory {
	public BalloonTemplateHandler createNewBalloonTemplateHandler(File templateFile, Connection connection);
	public BalloonTemplateHandler createNewBalloonTemplateHandler(String templateString, Connection connection);
}
