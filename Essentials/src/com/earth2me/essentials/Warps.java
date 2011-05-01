package com.earth2me.essentials;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Server;


public class Warps implements IConf
{
	private static final Logger logger = Logger.getLogger("Minecraft");
	Map<StringIgnoreCase, EssentialsConf> warpPoints = new HashMap<StringIgnoreCase, EssentialsConf>();
	File warpsFolder;
	Server server;

	public Warps(Server server, File dataFolder)
	{
		this.server = server;
		warpsFolder = new File(dataFolder, "warps");
		if (!warpsFolder.exists())
		{
			warpsFolder.mkdirs();
		}
		reloadConfig();
	}

	public boolean isEmpty()
	{
		return warpPoints.isEmpty();
	}

	public Iterable<String> getWarpNames()
	{
		List<String> keys = new ArrayList<String>();
		for (StringIgnoreCase stringIgnoreCase : warpPoints.keySet())
		{
			keys.add(stringIgnoreCase.string);
		}
		Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);
		return keys;
	}

	public Location getWarp(String warp) throws Exception
	{
		EssentialsConf conf = warpPoints.get(new StringIgnoreCase(warp));
		if (conf == null)
		{
			throw new Exception("That warp does not exist.");
		}
		return conf.getLocation(null, server);
	}

	public void setWarp(String name, Location loc) throws Exception
	{
		String filename = Util.sanitizeFileName(name);
		EssentialsConf conf = warpPoints.get(new StringIgnoreCase(name));
		if (conf == null)
		{
			File confFile = new File(warpsFolder, filename + ".yml");
			if (confFile.exists())
			{
				throw new Exception("A warp with a similar name already exists.");
			}
			conf = new EssentialsConf(confFile);
			warpPoints.put(new StringIgnoreCase(name), conf);
		}
		conf.setProperty(null, loc);
		conf.setProperty("name", name);
		conf.save();
	}

	public void delWarp(String name) throws Exception
	{
		EssentialsConf conf = warpPoints.get(new StringIgnoreCase(name));
		if (conf == null)
		{
			throw new Exception("Warp does not exist.");
		}
		if (!conf.getFile().delete())
		{
			throw new Exception("Problem deleting the warp file.");
		}
		warpPoints.remove(new StringIgnoreCase(name));
	}

	public final void reloadConfig()
	{
		warpPoints.clear();
		File[] listOfFiles = warpsFolder.listFiles();
		if (listOfFiles.length >= 1)
		{
			for (int i = 0; i < listOfFiles.length; i++)
			{
				String filename = listOfFiles[i].getName();
				if (listOfFiles[i].isFile() && filename.endsWith(".yml"))
				{
					EssentialsConf conf = new EssentialsConf(listOfFiles[i]);
					conf.load();
					String name = conf.getString("name");
					if (name != null)
					{
						warpPoints.put(new StringIgnoreCase(name), conf);
					}
				}
			}
		}
	}


	private class StringIgnoreCase
	{
		String string;

		public StringIgnoreCase(String string)
		{
			this.string = string;
		}

		@Override
		public int hashCode()
		{
			return string.toLowerCase().hashCode();
		}

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof String)
			{
				return string.equalsIgnoreCase((String)o);
			}
			if (o instanceof StringIgnoreCase)
			{
				return string.equalsIgnoreCase(((StringIgnoreCase)o).string);
			}
			return false;
		}
	}
}
