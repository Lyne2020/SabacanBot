package discordbot.sabacan;

import java.util.Map;

import discord4j.core.object.entity.MessageChannel;
import discordbot.sabacan.exceptions.DatabaseException;
import discordbot.sabacan.exceptions.GameServerException;
import discordbot.sabacan.exceptions.IPNotFoundException;

public class MinecraftService {

	private MinecraftService(){}
	private static final String gamename = "minecraft forge12.2";
	// 引数として渡されたBOTコマンドMAPに、このサービス用のコマンドを追加する。
	public static void registCommand(Map<String, Command> commands){

		commands.put("minecraft info", event -> {
			MessageChannel channel = event.getMessage().getChannel().block();
			channel.createMessage("--- mod導入について ---\n"
					+ "必要なmodのデータと導入手順は、http://lyneq.myqnapcloud.com にアクセスしたらDLできるよ。\n"
					+ "ユーザ名 / パスワード　：　public / Opek0sh!ne!t@!\n"
					+ "URLにアクセス後、shareアイコンを開いたらminecraftフォルダが見えるので、左の□にチェックを入れて上のスパナマークからダウンロードを選ぶとまとめてDL！\n"
					+ "\n--- サーバmod情報 ---\n"
					+ "forge 1.12.2 ・・・ あらゆるmodの前提mod。\n"
					+ "IndustrialCraft2 experimental 2.8.157 ・・・ 大規模工業化mod。高性能炉や発電機、バッテリーで駆動する近代工業的なアイテムを追加する。\n"
					+ "BuildCraft 7.99.24 ・・・ 大規模工業化mod。現代建築や近代設備の為の機械類を追加する。\n"
					+ "Additional Enchanted Miner 1.0.5 ・・・ BCの拡張mod。\n"
					+ "EnergyConverters 1.3.0 ・・・ RF・EU・Tesla・FE・MJを相互に変換できる機械を追加する。BCとIC2を相互接続するために必要。\n"
					+ "CutAll 2.5.2 ・・・ [C]ログイン時有効。斧で木をきると狙ったところから上がすべて伐採される。\n"
					+ "DigAll 2.3.2 ・・・ [G]ログイン時有効。スコップで地面を掘ると直径5ブロックがすべて掘削される。狙ったブロックより下は掘削されない。\n"
					+ "MineAll 2.6.5 ・・・ [M]ログイン時有効。つるはしで鉱石を掘ると直径3ブロックにある狙った鉱石と同じ種類の鉱石がすべて採掘される。\n"
					+ "StorageBox 3.2.0 ・・・ 1つのアイテムを無限に収納できるBOXを追加する。\n"
					+ "BedrockLayer 1.2.4 ・・・ 岩盤を平らにする。\n"
					+ "Just Enough Items 4.15.0 ・・・ レシピ確認ツール。\n"
					+ "InventoryTweaks 1.63 ・・・ インベントリ整理ツール。\n"
					+ "JourneyMap 5.5.5 ・・・ 高機能ミニマップmod。\n"
					+ "OptFine ・・・ 軽量化用mod。\n"
					+ "------\n").block();
		});

		commands.put("minecraft start", event -> {
    		MessageChannel channel = event.getMessage().getChannel().block();

    		try{
	    		channel.createMessage("了解ｯ！(･∀･)ゞ　" + gamename + "の鯖を立てるからすこし待ってね。\n 立ったらまたお知らせするよ。\n------").block();
	    		MachineOperator.startGameServer(gamename);
	    		MachineOperator.monitoringGameServer(gamename);
	    		channel.createMessage("鯖が立ったよ(/・ω・)/ \n 今データを復元してるから5分後くらいに下のIPアドレスに繋いでみて！").block();

	    		String infoResult = MachineOperator.getGameServerInfo(gamename);
	    		channel.createMessage("IPアドレス：" + infoResult + "\n\nあ、遊び終わったら「minecraft stop」って話しかけてね。").block();

    		}catch(DatabaseException dbe){channel.createMessage("既に開始されていたよ！TLを見てみてね").block();
    		}catch(IPNotFoundException ipe){channel.createMessage("IP情報の取得に失敗したよ。管理者に連絡してね(;'∀')").block();
    		}catch(Exception e){channel.createMessage("予期せぬエラー＞＜; 管理者に連絡してね(;'∀')").block();}
    	});

		commands.put("minecraft stop", event ->{
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
    		}catch(DatabaseException dbe){channel.createMessage("すでに停止されているよ！「minecraft start」って話しかけてもらえば起動させるよ！").block();
    		}catch(Exception e){channel.createMessage("予期せぬエラー＞＜; 管理者に連絡してね(;'∀')").block();
    		}
    	});
		commands.put("minecraft help", event -> {
			MessageChannel channel = event.getMessage().getChannel().block();
			channel.createMessage(""
					+ "--- マインクラフトサーバ コマンド---\n"
					+ "minecraft start ・・・ サーバを起動する。\n"
					+ "minecraft stop ・・・ サーバを停止する。\n"
					+ "minecraft info ・・・ プレイに必要なmodの情報を表示する。\n"
					+ "------").block();
		});
	}
}
