package net.thedarktide.celeo.darklock;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;

public class DarkLockCore extends JavaPlugin implements Listener
{

	private final Logger log = Logger.getLogger("Minecraft");
	private boolean inLockDown = false;
	private String denyMessage = "Server is in lockdown mode";

	@Override
	public void onEnable()
	{
		getDataFolder().mkdirs();
		if (!new File(getDataFolder(), "/config.yml").exists())
			saveDefaultConfig();
		inLockDown = getConfig().getBoolean("lockdown", false);
		denyMessage = getConfig().getString("message", "Server is in lockdown mode.");
		getServer().getPluginManager().registerEvents(this, this);
		log("Enabled.");
		log("Server is" + (inLockDown ? " " : " not ") + "in lockdown mode");
	}

	@Override
	public void onDisable()
	{
		getConfig().set("lockdown", inLockDown);
		getConfig().set("message", denyMessage);
		saveConfig();
		log("Disabled.");
	}

	public void log(String message)
	{
		log.info("[DarkLock] " + message);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if (player.isOp())
			{
				if (args == null || args.length == 0)
				{
					player.sendMessage("§c/lockdown [message|toggle]");
					return true;
				}
				String p = args[0].toLowerCase();
				if (p.equals("message"))
				{
					if (args.length == 1)
						player.sendMessage("§7Lockout message: §4"
								+ denyMessage);
					else
					{
						String change = "";
						for (int i = 1; i < args.length; i++)
						{
							if (change.equals(""))
								change = args[i];
							else
								change += " " + args[i];
						}
						denyMessage = change;
						player.sendMessage("§7Lockout message changed");
					}
					return true;
				}
				if (p.equals("toggle"))
				{
					inLockDown = !inLockDown;
					String message = "§eServer is"
							+ (inLockDown ? "§c " : "§a not ")
							+ "in lockdown mode";
					player.sendMessage(message);
					log(ChatColor.stripColor(message));
					getConfig().set("lockdown", Boolean.valueOf(inLockDown));
					saveConfig();
					return true;
				}
				player.sendMessage("§c/lockdown [message|toggle]");
				return true;
			}
			player.sendMessage("§cYou must be an operator to use this command");
			return true;
		}
		inLockDown = !inLockDown;
		log("Server is" + (inLockDown ? " " : " not ") + "in lockdown mode");
		return true;
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		if (!inLockDown)
			return;
		Player player = event.getPlayer();
		if (player.hasPermission("darklock.bypass")
				|| ApiLayer.hasPermission("world", CalculableType.USER, player.getName(), "darklock.bypass")
				|| player.isOp())
			return;
		event.disallow(Result.KICK_OTHER, denyMessage);
		log(player.getName() + " was kicked from the lockdown mode server: "
				+ denyMessage);
	}

}