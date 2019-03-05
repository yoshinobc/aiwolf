package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.evalmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.EventType;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAction;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAfterActionListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEvent;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameEventListener;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameTalk;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameVote;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.MetagameEventListener;

public class Village5TacEvalModel implements GameAfterActionListener, MetagameEventListener, GameEventListener {

	private CndlStrategy strategy;

	public Village5TacEvalModel(CndlStrategy strategy) {
		this.strategy = strategy;
		strategy.addPersistentAfterActionListener(this);
		strategy.addMetagameEventListener(this);
		strategy.addPersistentEventListener(this);
	}

	public class Village5Tactics {

		public boolean day2HasfakeCoSeer;// 占い騙りをしたことがある。
		public boolean day2VillagerHasCoPossessed;// 村人の時、狂人COしたことがある。
		public boolean day2VillagerHasCoWerewolf;// 村人の時、狼COしたことがある。
		public boolean day2SeerHasCoPossessed;// 占いの時、狂人COしたことがある。
		public boolean day2SeerHasCoWerewolf;// 占いの時、狼COしたことがある。
		public boolean day2PossessedHasCoPossessed;// 狂人の時、狂人COしたことがある。
		public boolean day2PossessedHasCoWerewolf;// 狂人の時、狼COしたことがある。
		public boolean day2WerewolfHasCoPossessed;// 狼の時、狂人COしたことがある。
		public boolean day2WerewolfHasCoWerewolf;// 狼の時、狼COしたことがある。
		public boolean day2PossessedHasVoteToWerewolf;// 狂人の時、単独狼COに投票したことがある。
		public boolean day2PossessedNotVoteToSeer;//狂人２日目の時、対抗の真占いに投票しない。
		public boolean day2WerewolfHasVoteToPossessed;// 狼の時、単独狂人COに投票したことがある。
		public boolean day2WerewolfHasAvoidWerewolf;// 狼の時、単独狼COに投票しなかったことがある。
		public boolean day2VillagerHasAvoidWerewolf;// 村人または占いの時、単独狼COに投票しなかったことがある。
		public boolean LiarSeerDivinedFakeBlack;//真占いの時、嘘の黒出しをしたことがある。
		public boolean LiarSeerDivinedFakeWhite;//真占いの時、嘘の白出しをしたことがある。
		public boolean HaveAvoidFakeBlack;//狂人の黒特攻に引っ掛からなかった。

	}

	private Map<Integer, Village5Tactics> tacitcsMap = new HashMap<>();

	public Village5Tactics getTacitcsMap(GameAgent agent) {
		return tacitcsMap.get(agent.getIndex());
	}

	@Override
	public void handleAfterAction(Game g, GameAction action, Content talk, GameAgent target) {
	}

	@Override
	public void handleEvent(Game g, GameEvent e) {

	}

	@Override
	public void startGame(Game g) {
		if (tacitcsMap.isEmpty()) {
			g.getAgentStream().forEach(ag -> tacitcsMap.put(ag.getIndex(), new Village5Tactics()));
		}
	}

	@Override
	public void endGame(Game g) {
		List<GameTalk> day2Talk = new ArrayList<>();
		for (GameEvent talkEvent : g.getEventAtDay(EventType.TALK, 2)) {
			day2Talk.addAll(talkEvent.talks);
		}
		List<GameTalk> day1Talk = new ArrayList<>();
		for (GameEvent talkEvent : g.getEventAtDay(EventType.TALK, 1)) {
			day1Talk.addAll(talkEvent.talks);
		}
		List<GameVote> day2Vote = new ArrayList<>();
		for (GameEvent voteEvent : g.getEventAtDay(EventType.VOTE, 2)) {
			day2Vote.addAll(voteEvent.votes);
		}
		GameAgent me = g.getSelf();
		for (GameAgent ag : g.getOthers()) {
			Village5Tactics tac = getTacitcsMap(ag);
			if (day2Talk.stream().anyMatch(talk -> talk.getTalker() == ag && talk.getTopic() == Topic.COMINGOUT
					&& talk.getRole() == Role.SEER)) {
				if(ag.role != Role.SEER) {
					tac.day2HasfakeCoSeer = true;
				}
			}
			// 各役職別、狂人COしたことがあるか記録
			if (day2Talk.stream().anyMatch(talk -> talk.getTalker() == ag && talk.getTopic() == Topic.COMINGOUT
					&& talk.getRole() == Role.POSSESSED)) {
				switch (ag.role) {
				case VILLAGER:
					tac.day2VillagerHasCoPossessed = true;
					//System.out.println("村人狂人CO:" + ag);
					break;
				case SEER:
					tac.day2SeerHasCoPossessed = true;
					//System.out.println("占い狂人CO:" + ag);
					break;
				case POSSESSED:
					tac.day2PossessedHasCoPossessed = true;
					//System.out.println("狂人狂人CO:" + ag);
					break;
				case WEREWOLF:
					tac.day2WerewolfHasCoPossessed = true;
					//System.out.println("狼狂人CO:" + ag);
					break;
				default:
					break;
				}
			}
			// 各役職別、狼COしたことがあるか記録
			if (day2Talk.stream().anyMatch(talk -> talk.getTalker() == ag && talk.getTopic() == Topic.COMINGOUT
					&& talk.getRole() == Role.WEREWOLF)) {
				switch (ag.role) {
				case VILLAGER:
					tac.day2VillagerHasCoWerewolf = true;
					//System.out.println("村人狼CO:" + ag);
					break;
				case SEER:
					tac.day2SeerHasCoWerewolf = true;
					//System.out.println("占い狼CO:" + ag);
					break;
				case POSSESSED:
					tac.day2PossessedHasCoWerewolf = true;
					//System.out.println("狂人狼CO:" + ag);
					break;
				case WEREWOLF:
					tac.day2WerewolfHasCoWerewolf = true;
					//System.out.println("狼狼CO:" + ag);
					break;
				default:
					break;
				}
			}
			// 狂人の時、単独狼COに投票したことがある。
			if (ag.role == Role.POSSESSED) {
				if (day2Vote.stream().filter(v -> v.initiator == ag).allMatch(v -> v.target.coRole == Role.WEREWOLF)) {
					List<GameAgent> WoCOs = day2Talk.stream()
							.filter(talk -> talk.getTopic() == Topic.COMINGOUT && talk.getRole() == Role.WEREWOLF)
							.map(talk -> talk.getTalker()).collect(Collectors.toList());
					if (WoCOs.size() == 1) {
						tac.day2PossessedHasVoteToWerewolf = true;
						//System.out.println("狂人vote狼:" + ag);
					}
				}
			}

			//狂人２日目、CO2の時に対抗に投票しない。
			if(ag.role == Role.POSSESSED) {
				if (day2Vote.stream().filter(v -> v.initiator == ag).allMatch(v -> v.target.role == Role.WEREWOLF)) {
					List<GameAgent>seerCOs = day1Talk.stream().filter(t -> t.getTopic() == Topic.COMINGOUT && t.getRole() == Role.SEER && t.getTalker() != ag).map(t -> t.getTalker()).collect(Collectors.toList());
					if(seerCOs.size() == 1) {
						if(seerCOs.get(0).isAlive) {
							tac.day2PossessedNotVoteToSeer = true;
						}
					}
				}
			}

			if (ag.role == Role.WEREWOLF) {
				// 狼の時、単独狂人COに投票したことがある。
				if (day2Vote.stream().filter(v -> v.initiator == ag).allMatch(v -> v.target.coRole == Role.POSSESSED)) {
					List<GameAgent> COs = day2Talk.stream()
							.filter(talk -> talk.getTopic() == Topic.COMINGOUT && talk.getRole() == Role.POSSESSED)
							.map(talk -> talk.getTalker()).collect(Collectors.toList());
					if (COs.size() == 1) {
						tac.day2WerewolfHasVoteToPossessed = true;
						//System.out.println("狼vote狂人:" + ag);
					}
				}
				// 狼の時、単独狼COに投票しなかったことがある。
				if (day2Vote.stream().filter(v -> v.initiator == ag).allMatch(v -> v.target.coRole != Role.WEREWOLF)) {
					List<GameAgent> COs = day2Talk.stream()
							.filter(talk -> talk.getTopic() == Topic.COMINGOUT && talk.getRole() == Role.WEREWOLF)
							.map(talk -> talk.getTalker()).collect(Collectors.toList());
					if (COs.size() == 1) {
						tac.day2WerewolfHasAvoidWerewolf = true;
						//System.out.println("狼Avoid狼:" + ag + "/t");
					}
				}
			}
			// 村人または占いの時、単独狼COに投票しなかったことがある。
			if (ag.role == Role.VILLAGER || ag.role == Role.SEER) {
				if (day2Vote.stream().filter(v -> v.initiator == ag).allMatch(v -> v.target.coRole != Role.WEREWOLF)) {
					List<GameAgent> COs = day2Talk.stream()
							.filter(talk -> talk.getTopic() == Topic.COMINGOUT && talk.getRole() == Role.WEREWOLF)
							.map(talk -> talk.getTalker()).collect(Collectors.toList());
					if (COs.size() == 1) {
						tac.day2VillagerHasAvoidWerewolf = true;
						//System.out.println("村人Avoid狼:" + ag);
					}
				}
			}

			//真占いの時に嘘の黒出しをしたことがある。
			if(ag.role == Role.SEER) {
				if(day1Talk.stream().filter(t -> t.getTalker() == ag && t.getTopic() == Topic.DIVINED && t.getResult() == Species.WEREWOLF).allMatch(t -> t.getTarget().role != Role.WEREWOLF)) {
					tac.LiarSeerDivinedFakeBlack = true;
				}
			}
			if(ag.role == Role.SEER) {
				if(day1Talk.stream().filter(t -> t.getTalker() == ag && t.getTopic() == Topic.DIVINED && t.getResult() == Species.HUMAN).allMatch(t -> t.getTarget().role == Role.WEREWOLF)) {
					tac.LiarSeerDivinedFakeWhite = true;
				}
			}

			//自分の黒特攻を回避したことがある。
			if(ag.role == Role.VILLAGER && me.role == Role.POSSESSED) {
				boolean flag = false;
				List<GameAgent>fakeblack = me.talkList.stream().filter(t -> t.getDay() == 1 && t.getTopic() == Topic.DIVINED && t.getResult() == Species.WEREWOLF).map(t -> t.getTarget()).collect(Collectors.toList());
				if(!fakeblack.isEmpty()) {
					List<GameAgent>targets = ag.voteList.stream().filter(v -> v.day == 1).map(v -> v.target).collect(Collectors.toList());
					if(!targets.isEmpty()) {
						for(GameAgent tg: targets) {
							if(fakeblack.contains(tg)) flag = true;
						}
					}
				}
				if(!flag)
					tac.HaveAvoidFakeBlack = true;
			}

		}
	}
}
