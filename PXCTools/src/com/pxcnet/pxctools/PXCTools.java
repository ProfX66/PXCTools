package com.pxcnet.pxctools;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

public class PXCTools extends JavaPlugin implements Listener {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	Set<String> ignoredCommands = new HashSet();
	@SuppressWarnings({ "unchecked", "rawtypes" })
	LinkedHashMap<Integer, String> joinCommands = new LinkedHashMap();
	PluginDescriptionFile pdfFile = this.getDescription();
	String pluginName = pdfFile.getName();
	String pluginVersion = pdfFile.getVersion().toString();
	String adminPermission = "pxctools.admin";
	String noPermission = "You have no power here!";
	public String logPrefix = ChatColor.GOLD + "[" + ChatColor.LIGHT_PURPLE + pluginName + ChatColor.GOLD + "]" + ChatColor.WHITE;
	String messageFormat;
	String unloadMsgFormat;
	String unloadMsgCountFormat;
	String sayMessageFormat;
	String discordCommand;
	String discordIgnore;
	String sleepFormat;
	String skippedNightMessage;
	String skippedStormMessage;
	String leftNightMessage;
	String leftStormMessage;
	String joinMessageText;
	Integer sleepPercent = 50;
	Boolean cancelMobTargeting = true;
	Boolean multiChatSupport = false;
	Boolean MultiverseSupport = false;
	Boolean ignoreSender = true;
	Boolean discordSupport = false;
	Boolean restrictInventoryMove = false;
	Boolean restrictItemPickup = false;
	Boolean RestrictItemDrop = false;
	Boolean DisableDispensers = false;
	Boolean DisableNoteBlockChange = false;
	Boolean DisableLeafDecay = false;
	Boolean clearInventory = false;
	Boolean showJoinMessage = false;
	Boolean RunOnRespawn = false;
	Boolean debug = false;
	MultiverseCore mvcore;
	MVWorldManager worldManager;
	
	@Override
	public void onEnable() 
	{
		log(String.format("v%1s has been enabled.", pluginVersion));
		this.loadConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}
	 
	@Override
	public void onDisable() 
	{
		System.out.println(String.format("%1s v%2s has been disabled.", pluginName, pluginVersion));
	}
	
	public void loadConfig()
	{
		ignoredCommands.clear();
		joinCommands.clear();

		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) { saveDefaultConfig(); }

		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(configFile);
			
			//Command Spy MultiChatSupport
			messageFormat = config.getString("Format").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			multiChatSupport = Boolean.parseBoolean(config.getString("MultiChatSupport"));
			ignoreSender = Boolean.parseBoolean(config.getString("IgnoreSender"));
			log("---- Command Spy Setting(s) ----");
			log(String.format("MessageFormat: %1s", messageFormat));
			log(String.format("IgnoreSender: %1s", ignoreSender));
			
			List<String> list = config.getStringList("IgnoredCommands");
			if (list != null)
			{
				for (String s : list) 
				{
					log(String.format("Ignored Command: %1s", s.toLowerCase()));
					ignoredCommands.add(s.toLowerCase());
				}
			}
			log(" ");
			
			//Replacement for default say command prefix
			sayMessageFormat = config.getString("SayFormat").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			discordSupport = Boolean.parseBoolean(config.getString("DiscordSRVSupport"));
			discordCommand = config.getString("DiscordSRVCommand");
			discordIgnore = config.getString("DiscordIgnorePattern");
			log("---- Say Replacement Setting(s) ----");
			log(String.format("SayFormat: %1s", sayMessageFormat));
			log(String.format("DiscordSRVSupport: %1s", discordSupport));
			log(String.format("DiscordSRVCommand: %1s", discordCommand));
			log(String.format("DiscordIgnorePattern: %1s", discordIgnore));
			log(" ");
			
			//Percentage Sleep
			sleepFormat = config.getString("SleepFormat").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			sleepPercent = Integer.parseInt(config.getString("SleepPercentage"));
			cancelMobTargeting = Boolean.parseBoolean(config.getString("CancelMobTargeting"));
			skippedNightMessage = config.getString("SkippedNightMessage").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			skippedStormMessage = config.getString("SkippedStormMessage").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			leftNightMessage = config.getString("LeftNightMessage").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			leftStormMessage = config.getString("LeftStormMessage").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			log("---- Percentage Sleep Setting(s) ----");
			log(String.format("SleepFormat: %1s", sleepFormat));
			log(String.format("SleepPercentage: %1s%%", sleepPercent));
			log(String.format("CancelMobTargeting: %1s", cancelMobTargeting));
			log(String.format("SkippedNightMessage: %1s", skippedNightMessage));
			log(String.format("SkippedStormMessage: %1s", skippedStormMessage));
			log(String.format("LeftNightMessage: %1s", leftNightMessage));
			log(String.format("LeftStormMessage: %1s", leftStormMessage));
			log(" ");
			
			//Inventory Management
			restrictInventoryMove = Boolean.parseBoolean(config.getString("RestrictInventoryMove"));
			restrictItemPickup = Boolean.parseBoolean(config.getString("RestrictItemPickup"));
			RestrictItemDrop = Boolean.parseBoolean(config.getString("RestrictItemDrop"));
			log("---- Inventory Management Setting(s) ----");
			log(String.format("RestrictInventoryMove: %1s", restrictInventoryMove));
			log(String.format("RestrictItemPickup: %1s", restrictItemPickup));
			log(String.format("RestrictItemDrop: %1s", RestrictItemDrop));
			log(" ");
			
			//World Management
			DisableDispensers = Boolean.parseBoolean(config.getString("DisableDispensers"));
			DisableNoteBlockChange = Boolean.parseBoolean(config.getString("DisableNoteBlockChange"));
			DisableLeafDecay = Boolean.parseBoolean(config.getString("DisableLeafDecay"));
			log("---- World Management Setting(s) ----");
			log(String.format("DisableDispensers: %1s", DisableDispensers));
			log(String.format("DisableNoteBlockChange: %1s", DisableNoteBlockChange));
			log(String.format("DisableLeafDecay: %1s", DisableLeafDecay));
			log(" ");
			
			//Multiverse
			log("---- Checking for Multiverse ----");
			MultiverseSupport = Boolean.parseBoolean(config.getString("MultiverseSupport"));
			
			if (MultiverseSupport)
			{
				unloadMsgFormat = config.getString("UnloadWorldMsgFormat").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
				unloadMsgCountFormat = config.getString("UnloadWorldMsgCountFormat").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
				log(String.format("UnloadWorldMsgFormat: %1s", unloadMsgFormat));
				log(String.format("UnloadWorldMsgFormat: %1s", unloadMsgCountFormat));
				
				mvcore = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
				log(String.format("MultiverseCore Enabled: %1s", mvcore.isEnabled()));
				if (mvcore.isEnabled())
				{
					worldManager = mvcore.getMVWorldManager();
					log(String.format("Found worlds: %1s", worldManager.getMVWorlds().size()));
				}
			}
			else { log("Skipping Multiverse hooks..."); }
			log(" ");
			
			//Player Join
			clearInventory = Boolean.parseBoolean(config.getString("ClearInventory"));
			showJoinMessage = Boolean.parseBoolean(config.getString("ShowJoinMessage"));
			RunOnRespawn = Boolean.parseBoolean(config.getString("RunOnRespawn"));
			joinMessageText = config.getString("JoinMessageText").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			log("---- Player Join Setting(s) ----");
			log(String.format("ClearInventory: %1s", clearInventory));
			log(String.format("ShowJoinMessage: %1s", showJoinMessage));
			log(String.format("JoinMessageText: %1s", joinMessageText));
			log(String.format("RunOnRespawn: %1s", RunOnRespawn));
			
			List<String> cmdList = config.getStringList("CommandsToRun");
			Integer corder = 1;
			if (cmdList != null)
			{
				for (String s : cmdList) 
				{
					log(String.format("Join Command #%1s: %2s", corder, s));
					joinCommands.put(corder, s);
					++corder;
				}
			}
			log(" ");

			//Debug
			debug = Boolean.parseBoolean(config.getString("DebugMode"));
			log("---- Debug Setting(s) ----");
			log(String.format("DebugMode: %1s", debug));
			
			
		}
		catch (Exception e)
		{
			getLogger().severe("CONFIG FAILED TO LOAD");
			e.printStackTrace();
		}
	}
	
	public void showhelp(CommandSender sender)
	{
		String cmdPrefix = String.format("/%1s ", pluginName.toLowerCase());
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "-----{ " + ChatColor.GOLD + pluginName + ChatColor.LIGHT_PURPLE + " - " + ChatColor.GOLD + "v" + pluginVersion + ChatColor.LIGHT_PURPLE + " }-----");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "- " + ChatColor.GOLD + cmdPrefix + "help" + ChatColor.LIGHT_PURPLE + " - " + ChatColor.WHITE + "Shows this help");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "- " + ChatColor.GOLD + cmdPrefix + "join [Username]" + ChatColor.LIGHT_PURPLE + " - " + ChatColor.WHITE + "Runs the configured join actions");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "- " + ChatColor.GOLD + cmdPrefix + "reload" + ChatColor.LIGHT_PURPLE + " - " + ChatColor.WHITE + "Reloads the config");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "- " + ChatColor.GOLD + cmdPrefix + "unloadworlds" + ChatColor.LIGHT_PURPLE + " - " + ChatColor.WHITE + "Unloads worlds with no players");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		Boolean isPlayer = (sender instanceof Player);
		Boolean hasArgs = (args.length > 0);
		Boolean hasPermission = (sender.isOp()) || (sender.hasPermission(adminPermission));
		String recievedCommand = command.getName().toLowerCase();

		if (debug)
		{
			log("----------------");
			log("isPlayer: " + isPlayer);
			log("args: " + String.join("; ", args));
			log("argslength : " + args.length);
			log("hasArgs: " + hasArgs);
			log("hasPermission: " + hasPermission);
			log("recievedCommand: " + recievedCommand);
			log("----------------");
		}
		
		if (!hasPermission)
		{
			sender.sendMessage(ChatColor.LIGHT_PURPLE + pluginName + ": " + ChatColor.RED + noPermission);
			return false;
		}
		
		switch(recievedCommand)
		{
			case "pxctools":
				if (!hasArgs) { showhelp(sender); }
				else
				{
					switch(args[0])
					{
						case "help":
							showhelp(sender);
							break;
						case "reload":
							this.loadConfig();
							sender.sendMessage(ChatColor.LIGHT_PURPLE + pluginName + " v" + pluginVersion + " reloaded!");
							break;
						case "join":
							if (isPlayer)
							{
								Player player = (Player)sender;
								if (args.length == 1) { doPlayerJoin(player); }
								else
								{
									List<String> argList = Arrays.asList(args);
									for (Player p : Bukkit.getOnlinePlayers())
									{
										if (argList.contains(p.getName())) { doPlayerJoin(p); }
									}
								}
							}
							else { log(ChatColor.RED + "That command is only for a player - Console cannot use it!"); }
							break;
						case "unloadworlds":
							unloadWorlds();
							break;
						default:
							showhelp(sender);
							break;
					}
				}
				break;
			case "say":
				if (!hasArgs) { sender.sendMessage(ChatColor.LIGHT_PURPLE + "/" + recievedCommand + " " + ChatColor.RED + "(Message)"); return false; }
				else
				{
					String strBuild = "";
					for (int i = 0; i < args.length; i++)
					{
						strBuild = strBuild + args[i] + " ";
					}
					String finalMessage = String.format("%s %s", sayMessageFormat, strBuild);
					Bukkit.broadcastMessage(finalMessage);
					if (discordSupport)
					{  
						log(String.format("matches discordIgnore: %1s", finalMessage.matches(discordIgnore)));
						if (!finalMessage.matches(discordIgnore))
						{
							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), discordCommand.replace("%message%", finalMessage));
						}
					}
				}
				break;
			default:
				return false;
		}
		
		return true;
	}
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event)
	{
		Player player = event.getPlayer();
		String command = event.getMessage().split(" ")[0].substring(1).toLowerCase();
		
		//if (multiChatSupport)
		
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if ((p.isOp()) || (p.hasPermission(adminPermission)) || (p.hasPermission("pxctools.spy")))
			{
				Boolean skipPlayer = (p == player);
				if (!ignoreSender) { skipPlayer = false; }
				if (ignoredCommands.contains(command)) { skipPlayer = true; }
				if (!skipPlayer)
				{
					String strBuild = messageFormat.replace("%player%", player.getName());
					strBuild = strBuild.replace("%command%", event.getMessage());
					p.sendMessage(strBuild);
				}
			}
		}
	}
	
	public void unloadWorlds()
	{
		Bukkit.broadcastMessage(unloadMsgFormat);
		Integer unCount = 0;
		
		for (MultiverseWorld world : worldManager.getMVWorlds())
		{
			if (world.getName().equalsIgnoreCase(worldManager.getSpawnWorld().getName()))
			{
				log("Loaded world [ " + world.getColoredWorldString() + " ] is the spawn world - Skipping...");
			}
			else
			{
				List<Player> playerList = Bukkit.getOnlinePlayers().stream().filter(onlinePlayer -> onlinePlayer.getWorld().getName() == world.getName()).collect(Collectors.toList());
				if (playerList.size() > 0)
				{
					log("Loaded world [ " + world.getColoredWorldString() + " ] has [ " + playerList.size() + " ] player(s) - Skipping...");
				}
				else
				{
					log("Loaded world [ " + world.getColoredWorldString() + " ] has [ " + playerList.size() + " ] player(s) - Unloading...");
					if (worldManager.unloadWorld(world.getName()))
					{
						log("Successfully unloaded world.");
						++unCount;
					}
					else { log("Could not unload world!"); }
				}
			}
		}
		
		Bukkit.broadcastMessage(unloadMsgCountFormat.replace("%count%", unCount.toString()));
	}
	
	public void log(String Message)
	{
		System.out.println(logPrefix + " " + Message);
	}
	
	@EventHandler
	public void onMobTarget(EntityTargetLivingEntityEvent event)
	{
	    if (!(event.getTarget() instanceof Player)) { return; }
	    Player player = (Player)event.getTarget();
	    
	    if (player.isSleeping() && cancelMobTargeting)
	      event.setCancelled(true); 
	}

	float percentageRequired;
	float admPlayers;
	float onlinePlayers;
	int sleepingPlayers;
	float percentageSleeping;
	float playersSleepRequired;

	@EventHandler
	public void onPlayerSleep(PlayerBedEnterEvent e)
	{
	    World world = (World)Bukkit.getWorlds().get(0);
	    
	    List<Player> AdminPlayers = Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission(adminPermission)).collect(Collectors.toList());
	    List<Player> Players = Bukkit.getOnlinePlayers().stream().filter(p -> (p.getWorld() == world)).collect(Collectors.toList());
	    List<Player> SleepingPlayers = Bukkit.getOnlinePlayers().stream().filter(p -> p.isSleeping()).collect(Collectors.toList());
	    
	    this.percentageRequired = sleepPercent;
	    this.admPlayers = AdminPlayers.size();
	    this.onlinePlayers = Players.size();
	    this.sleepingPlayers = SleepingPlayers.size() + 1;
	    float adjustedUsers = onlinePlayers - admPlayers;
	    this.playersSleepRequired = (int)Math.ceil((adjustedUsers / 100.0F * this.percentageRequired));
	    this.percentageSleeping = this.sleepingPlayers / adjustedUsers * 100.0F;
	    
	    if (debug)
	    {
	    	log("----------------");
	    	log("percentageRequired: " + percentageRequired);
		    log("admPlayers: " + admPlayers);
		    log("onlinePlayers: " + onlinePlayers);
		    log("adjustedUsers: " + adjustedUsers);
		    log("sleepingPlayers: " + sleepingPlayers);
		    log("playersSleepRequired: " + playersSleepRequired);
		    log("percentageSleeping: " + percentageSleeping);
		    log("----------------");
	    }
	    
	    Player pl = e.getPlayer();

	    if (pl.getWorld() != world)
	    {
	      return;
	    }

	    if (!isNight(world) && !world.isThundering())
	    {
	      return;
	    }
	    
	    for (Player player : Players) 
	    {
	    	if (isNight(world))
	    	{
	    		sendPlayersLeftMessage(player, leftNightMessage); continue;
	    	} 
	    		sendPlayersLeftMessage(player, leftStormMessage);
	    } 
	
	    if (!percentageRequired())
	    {
	      return;
	    }
	    
	    for (Player p : Players)
	    {
	    	if (isNight(p.getWorld()))
	    	{
	    		p.sendMessage(skippedNightMessage); continue;
	    	} 
	    	p.sendMessage(skippedStormMessage);
	    } 
	
	    if (isNight(world))
	    {
	    	world.setTime(0L);
	    	if (world.isThundering())
	    		world.setThundering(false); 
	    	world.setStorm(false);
	    } else 
	    {
	      world.setThundering(false);
	      world.setStorm(false);
	    } 
	}

	private boolean isNight(World world) { return (world.getTime() > 12541L && world.getTime() < 23850L); }
	
	private boolean percentageRequired() { return (this.percentageSleeping >= this.percentageRequired); }
	
	private void sendPlayersLeftMessage(Player player, String message)
	{
		message = sleepFormat + " " + message;
		message = message.replaceFirst("%sleeping%", Integer.toString(this.sleepingPlayers));
		message = message.replaceAll("%required%", Integer.toString(Math.round(this.playersSleepRequired)));
		message = message.replaceAll("%player%", player.getName());
	    player.sendMessage(message);
	}
	
	@EventHandler
    public void JoinEvent(PlayerJoinEvent event)
    {
		if (event.getPlayer() != null)
		{
			doPlayerJoin(event.getPlayer());
		}
    }
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if (!(RunOnRespawn)) { return; }
		if (event.getPlayer() != null)
		{
			Player player = event.getPlayer();
			Boolean hasPermission = (player.isOp()) || (player.hasPermission(adminPermission));
			if (!hasPermission) { doPlayerJoin(player); }
		}
	}
	
	private void doPlayerJoin(Player player)
	{
		Boolean hasPermission = (player.isOp()) || (player.hasPermission(adminPermission));
		log("User [ " + player.getName().toString() + " ] has joined with op/admin permissions [ " + hasPermission.toString() + " ]");
		
		if (!hasPermission)
		{
			if (clearInventory)
			{
				log("ClearInventory is [ " + clearInventory.toString() + " ] - Clearing inventory...");
				player.getInventory().clear();
				player.updateInventory();
			}
		}
		
		for (String cmd : joinCommands.values())
		{
			if (cmd.startsWith("All:"))
			{
				String eCmd = cmd.replaceFirst("^All:", "").strip();
				if (eCmd.startsWith("Console:"))
				{
					eCmd = eCmd.replaceFirst("^Console:", "").strip();
					eCmd = eCmd.replace("%player%", player.getName());
					RunConsoleCommand(eCmd);
				}
				else { RunPlayerCommand(player, eCmd); }
			}
			else if (cmd.startsWith("Console:"))
			{
				if (!hasPermission)
				{
					String eCmd = cmd.replaceFirst("^Console:", "").strip();
					eCmd = eCmd.replace("%player%", player.getName());
					RunConsoleCommand(eCmd);
				}
			}
			else
			{
				if (!hasPermission) { RunPlayerCommand(player, cmd.strip()); }
			}
		}
		
		if (showJoinMessage)
		{
			if (debug) { log("Sending join message: " + joinMessageText); }
			player.sendMessage(joinMessageText);
		}
	}
	
	public void RunPlayerCommand(Player player, String cmd)
	{
		Command command = Bukkit.getServer().getPluginCommand(cmd.split(" ")[0].toLowerCase());
		if (debug)
		{
			log("Full command line to run: '" + cmd + "'");
			log("Command: '" + cmd.split(" ")[0].toLowerCase() + "'");
			if (command != null) { log("Command is valid - Running as player..."); }
			else { log("Command is not valid - Skipping..."); }
		}

		if (command != null) { player.performCommand(cmd); }
	}
	
	public void RunConsoleCommand(String cmd)
	{
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		Command command = Bukkit.getServer().getPluginCommand(cmd.split(" ")[0].toLowerCase());
		if (debug)
		{
			log("Full command line to run: '" + cmd + "'");
			log("Command: '" + cmd.split(" ")[0].toLowerCase() + "'");
			if (command != null) { log("Command is valid - Running as console..."); }
			else { log("Command is not valid - Skipping..."); }
		}

		if (command != null) { Bukkit.dispatchCommand(console, cmd); }
	}
	
	@EventHandler
    public void inventory(InventoryClickEvent event)
    { 
		if (restrictInventoryMove)
		{
			HumanEntity user = event.getWhoClicked();
	    	Boolean hasPermission = (user.isOp()) || (user.hasPermission(adminPermission));
	    	if (!hasPermission)
	    	{
	    		if (debug) { log("(DBG) User [ " + user.getName() + " ] attempted to change their inventory."); }
	    		event.setCancelled(true);
	    	}
		}
    }
	
	@EventHandler
	public void ItemPickup(EntityPickupItemEvent event)
	{
		if (restrictItemPickup)
		{
			LivingEntity ent = event.getEntity();
			Boolean hasPermission = (ent.isOp()) || (ent.hasPermission(adminPermission));
	    	if (!hasPermission)
	    	{
	    		if (debug) { log("(DBG) Entity [ " + ent.getName() + " ] attempted to pickup an item."); }
	    		event.setCancelled(true);
	    	}
		}
	}
	
	@EventHandler
	public void StopDispenser(BlockDispenseEvent event)
	{
		if (DisableDispensers)
		{
			Block block = event.getBlock();
			Material mBlock = block.getType();
			Location lBlock = block.getLocation();;
			Material mItem = event.getItem().getType();
			
			String blockCoords = lBlock.getWorld().getName() + ", " + lBlock.getBlockX() + ", " + lBlock.getBlockY() + ", " + lBlock.getBlockZ();
			String blockType = mBlock.toString();
			String itemType = mItem.toString();
			
			if (debug) { log("(DBG) Block [ " + blockType + " ] at [ " + blockCoords + " ] attempted to dispense [ " + itemType + " ]"); }
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void ItemDrop(PlayerDropItemEvent event)
	{
		if (RestrictItemDrop)
		{
			Player player = event.getPlayer();
			Boolean hasPermission = (player.isOp()) || (player.hasPermission(adminPermission));
	    	if (!hasPermission)
	    	{
	    		if (debug) { log("(DBG) Entity [ " + player.getName() + " ] attempted to drop an item."); }
	    		event.setCancelled(true);
	    	}
		}
	}
	
	@EventHandler
	public void StopNoteBlockChange(PlayerInteractEvent event)
	{
		if (DisableNoteBlockChange)
		{
			Action action = event.getAction();
			if (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR))
				return;
			
			Player player = event.getPlayer();
			Boolean hasPermission = (player.isOp()) || (player.hasPermission(adminPermission));
	    	if (!hasPermission)
	    	{
				Material block = event.getClickedBlock().getType();
				if (action.equals(Action.RIGHT_CLICK_BLOCK) && block.equals(Material.NOTE_BLOCK))
				{
					if (debug) { log("(DBG) Entity [ " + player.getName() + " ] attempted to change a noteblock."); }
		    		event.setCancelled(true);
				}
	    	}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onLeavesDecay(LeavesDecayEvent event)
	{
		if (DisableLeafDecay)
		{
			event.setCancelled(true);
		}
	}
}
