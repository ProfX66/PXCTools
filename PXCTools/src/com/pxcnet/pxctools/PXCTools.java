package com.pxcnet.pxctools;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class PXCTools extends JavaPlugin implements Listener {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	Set<String> ignoredCommands = new HashSet();
	@SuppressWarnings({ "unchecked", "rawtypes" })
	Set<String> joinCommands = new HashSet();
	PluginDescriptionFile pdfFile = this.getDescription();
	String pluginName = pdfFile.getName();
	String pluginVersion = pdfFile.getVersion().toString();
	String adminPermission = "pxctools.admin";
	String noPermission = "You have no power here!";
	String messageFormat;
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
	Boolean ignoreSender = true;
	Boolean discordSupport = false;
	Boolean restrictInventoryMove = false;
	Boolean clearInventory = false;
	Boolean showJoinMessage = false;
	Boolean debug = false;
	
	@Override
	public void onEnable() 
	{
		System.out.println(String.format("%1s v%2s has been enabled.", pluginName, pluginVersion));
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
			log(String.format("[%1s] ---- Command Spy Setting(s) ----", pluginName));
			log(String.format("[%1s] MessageFormat: %2s", pluginName, messageFormat));
			log(String.format("[%1s] IgnoreSender: %2s", pluginName, ignoreSender));
			
			List<String> list = config.getStringList("IgnoredCommands");
			if (list != null)
			{
				for (String s : list) 
				{
					log(String.format("[%1s] Ignored Command: %2s", pluginName, s.toLowerCase()));
					ignoredCommands.add(s.toLowerCase());
				}
			}
			
			//Replacement for default say command prefix
			sayMessageFormat = config.getString("SayFormat").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			discordSupport = Boolean.parseBoolean(config.getString("DiscordSRVSupport"));
			discordCommand = config.getString("DiscordSRVCommand");
			discordIgnore = config.getString("DiscordIgnorePattern");
			log(String.format("[%1s] ---- Say Replacement Setting(s) ----", pluginName));
			log(String.format("[%1s] SayFormat: %2s", pluginName, sayMessageFormat));
			log(String.format("[%1s] DiscordSRVSupport: %2s", pluginName, discordSupport));
			log(String.format("[%1s] DiscordSRVCommand: %2s", pluginName, discordCommand));
			log(String.format("[%1s] DiscordIgnorePattern: %2s", pluginName, discordIgnore));
			
			//Percentage Sleep
			sleepFormat = config.getString("SleepFormat").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			sleepPercent = Integer.parseInt(config.getString("SleepPercentage"));
			cancelMobTargeting = Boolean.parseBoolean(config.getString("CancelMobTargeting"));
			skippedNightMessage = config.getString("SkippedNightMessage").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			skippedStormMessage = config.getString("SkippedStormMessage").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			leftNightMessage = config.getString("LeftNightMessage").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			leftStormMessage = config.getString("LeftStormMessage").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			log(String.format("[%1s] ---- Percentage Sleep Setting(s) ----", pluginName));
			log(String.format("[%1s] SleepFormat: %2s", pluginName, sleepFormat));
			log(String.format("[%1s] SleepPercentage: %2s%%", pluginName, sleepPercent));
			log(String.format("[%1s] CancelMobTargeting: %2s", pluginName, cancelMobTargeting));
			log(String.format("[%1s] SkippedNightMessage: %2s", pluginName, skippedNightMessage));
			log(String.format("[%1s] SkippedStormMessage: %2s", pluginName, skippedStormMessage));
			log(String.format("[%1s] LeftNightMessage: %2s", pluginName, leftNightMessage));
			log(String.format("[%1s] LeftStormMessage: %2s", pluginName, leftStormMessage));
			
			//Inventory Management
			restrictInventoryMove = Boolean.parseBoolean(config.getString("RestrictInventoryMove"));
			log(String.format("[%1s] ---- Inventory Management Setting(s) ----", pluginName));
			log(String.format("[%1s] RestrictInventoryMove: %2s", pluginName, restrictInventoryMove));
			
			//Player Join
			clearInventory = Boolean.parseBoolean(config.getString("ClearInventory"));
			showJoinMessage = Boolean.parseBoolean(config.getString("ShowJoinMessage"));
			joinMessageText = config.getString("JoinMessageText").replaceAll("&([a-zA-Z0-9])", "\u00A7$1");
			log(String.format("[%1s] ---- Player Join Setting(s) ----", pluginName));
			log(String.format("[%1s] ClearInventory: %2s", pluginName, clearInventory));
			log(String.format("[%1s] ShowJoinMessage: %2s", pluginName, showJoinMessage));
			log(String.format("[%1s] JoinMessageText: %2s", pluginName, joinMessageText));
			
			List<String> cmdList = config.getStringList("CommandsToRun");
			if (cmdList != null)
			{
				for (String s : cmdList) 
				{
					log(String.format("[%1s] Join Command: %2s", pluginName, s));
					joinCommands.add(s);
				}
			}
			
			//Debug
			debug = Boolean.parseBoolean(config.getString("DebugMode"));
			log(String.format("[%1s] ---- Debug Setting(s) ----", pluginName));
			log(String.format("[%1s] DebugMode: %2s", pluginName, debug));
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
	}
	
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		Player player = (Player)sender;
		Boolean hasArgs = (args.length > 0);
		Boolean hasPermission = (sender.isOp()) || (sender.hasPermission(adminPermission));
		String recievedCommand = command.getName().toLowerCase();

		if (debug)
		{
			String logPrefix = "[" + pluginName + "] ";
			log(logPrefix + "args: " + String.join("; ", args));
			log(logPrefix + "argslength : " + args.length);
			log(logPrefix + "hasArgs: " + hasArgs);
			log(logPrefix + "hasPermission: " + hasPermission);
			log(logPrefix + "recievedCommand: " + recievedCommand);
			log(logPrefix + "----------------");
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
							if (args.length == 1) { doPlayerJoin(player); }
							else
							{
								List<String> argList = Arrays.asList(args);
								for (Player p : Bukkit.getOnlinePlayers())
								{
									if (argList.contains(p.getName())) { doPlayerJoin(p); }
								}
							}
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
						log(String.format("[%1s] matches discordIgnore: %2s", pluginName, finalMessage.matches(discordIgnore)));
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
			if ((p.isOp()) || (p.hasPermission(adminPermission)))
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
	
	public void log(String Message)
	{
		System.out.println(Message);
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
	    	String logPrefix = "[" + pluginName + "] ";
	    	log(logPrefix + "percentageRequired: " + percentageRequired);
		    log(logPrefix + "admPlayers: " + admPlayers);
		    log(logPrefix + "onlinePlayers: " + onlinePlayers);
		    log(logPrefix + "adjustedUsers: " + adjustedUsers);
		    log(logPrefix + "sleepingPlayers: " + sleepingPlayers);
		    log(logPrefix + "playersSleepRequired: " + playersSleepRequired);
		    log(logPrefix + "percentageSleeping: " + percentageSleeping);
		    log(logPrefix + "----------------");
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
    	doPlayerJoin(event.getPlayer());
    }
	
	private void doPlayerJoin(Player player)
	{
		Boolean hasPermission = (player.isOp()) || (player.hasPermission(adminPermission));
		
		if (!hasPermission)
		{
			if (clearInventory)
			{
				player.getInventory().clear();
				player.updateInventory();
			}
			
			for (String cmd : joinCommands)
			{
				Command command = Bukkit.getServer().getPluginCommand(cmd.split(" ")[0].substring(1).toLowerCase());
				if (command != null) { player.performCommand(cmd); }
			}
			
			if (showJoinMessage) { player.sendMessage(joinMessageText); }
		}
	}
	
	@EventHandler
    public void inventory(InventoryClickEvent event)
    { 
		if (restrictInventoryMove)
		{
			HumanEntity user = event.getWhoClicked();
	    	Boolean hasPermission = (user.isOp()) || (user.hasPermission(adminPermission));
	    	if (!hasPermission) { event.setCancelled(true); }
		}
    }
}
