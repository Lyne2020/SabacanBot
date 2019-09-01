package discordbot.sabacan;

import java.util.Map;

import discord4j.core.object.entity.MessageChannel;
import discordbot.sabacan.exceptions.DatabaseException;
import discordbot.sabacan.exceptions.GameServerException;
import discordbot.sabacan.exceptions.IPNotFoundException;

public class SevenDaysToDieService {
	private SevenDaysToDieService(){}
	private static final String gamename = "7days to die";
	// 引数として渡されたBOTコマンドMAPに、このサービス用のコマンドを追加する。
	public static void registCommand(Map<String, Command> commands){
		commands.put("7dtd help", event -> {
			MessageChannel channel = event.getMessage().getChannel().block();
			channel.createMessage(""
					+ "--- 7Days to Dieサーバ コマンド---\n"
					+ "7dtd start ・・・ サーバを起動する。\n"
					+ "7dtd stop ・・・ サーバを停止する。\n"
					+ "------").block();
		});
		commands.put("7dtd start", event -> {
			MessageChannel channel = event.getMessage().getChannel().block();

    		try{
	    		channel.createMessage("了解ｯ！(･∀･)ゞ　" + gamename + "の鯖を立てるからすこし待ってね。\n 立ったらまたお知らせするよ。\n------").block();
	    		MachineOperator.startGameServer(gamename);
	    		MachineOperator.monitoringGameServer(gamename);
	    		channel.createMessage("鯖が立ったよ(/・ω・)/ \n 今データを復元してるから5分後くらいに下のIPアドレスに繋いでみて！").block();

	    		String infoResult = MachineOperator.getGameServerInfo(gamename);
	    		channel.createMessage("IPアドレス：" + infoResult + "\n\nあ、遊び終わったら「7dtd stop」って話しかけてね。").block();

    		}catch(DatabaseException dbe){channel.createMessage("既に開始されているよ！TLを見てみてね").block();
    		}catch(IPNotFoundException ipe){channel.createMessage("IP情報の取得に失敗したよ。管理者に連絡してね(;'∀')").block();
    		}catch(Exception e){channel.createMessage("予期せぬエラー＞＜; 管理者に連絡してね(;'∀')").block();}
    	});

		commands.put("7dtd stop", event ->{
    		MessageChannel channel = event.getMessage().getChannel().block();

    		try{
    		channel.createMessage("了解ｯ！(･∀･)ゞ　今から" + gamename + "のサービスを停止するね。\n 止めたらまたお知らせするよ。\n------").block();
    		MachineOperator.stopGameService(gamename);
	    	channel.createMessage("サービスを停止したよ。あと2分後に鯖も停止するよ。").block();

    		// 2分停止する
    		Thread.sleep(120000);

    		MachineOperator.stopGameServer(gamename);
			channel.createMessage("鯖を停止したよ。また遊んでね！").block();
    		}catch(GameServerException gse){channel.createMessage("サービスの停止に失敗したよ。管理者に連絡してね(;'∀')").block();
    		}catch(DatabaseException dbe){channel.createMessage("すでに停止されていたよ！「7dtd start」って話しかけてもらえば起動させるよ！").block();
    		}catch(Exception e){channel.createMessage("予期せぬエラー＞＜; 管理者に連絡してね(;'∀')").block();
    		}
    	});
	}
}
