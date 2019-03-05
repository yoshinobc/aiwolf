package jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.target;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.CndlStrategy;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2018impl.framework.GameAgent;

public class AttackLastDay extends CndlTargetTactic {

	private Set<GameAgent> seers = new HashSet<>();
	private Set<GameAgent> possesseds = new HashSet<>();

	@Override
	public GameAgent targetImpl(Game game, CndlStrategy strategy) {
		GameAgent me = game.getSelf();
		List<GameAgent> others = game.getAliveOthers();
		GameAgent target = others.get(0);
		return target;

	}
}
