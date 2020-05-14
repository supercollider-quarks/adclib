/*
(
// speedlimit sketch with object modeling
~speedLimit = (
	action: { |...args| args.postln },
	dt: 0.1,
	scheduled: false,
	lastTime: Main.elapsedTime,
	filterValue: { |dict ...args|
		var now = Main.elapsedTime;
		var delta = now - dict.lastTime;
		var doitnow = delta > dict.dt;
		if (doitnow) {
			dict[\action].value(*args);
			dict.lastTime = now;
		} {
			dict[\latestArgs] = args;
			if (dict.scheduled.not) {
				dict.scheduled = true;
				SystemClock.sched(dict.dt - delta) {
					dict[\action].value(*dict[\latestArgs]);
					dict.lastTime = Main.elapsedTime;
					dict.scheduled = false;
				}
			}
		};
	};
);
// and test for it
fork {
	(5..100).mirror.do { |i|
		~speedLimit.filterValue(i);
		(1 / i).wait;
	}
};
)

(
// test for SpeedLimit class:
// posts indices of sent messages,
// and time and args for actually evaluated calls;
// should always do very last message too.

a = SpeedLimit({ |...args|
[ Main.elapsedTime - a.lastTime, args ].postln
}, 0.1);
fork {
	(5..100).mirror.do { |i|
		i.postln;
		a.filterValue(i);
		(1 / i).wait;
	}
};
)
*/

SpeedLimit {
	var <>action, <>dt;
	var <now, <lastTime, <delta, <scheduled = false, <latestArgs;

	*new { |action, dt = 0.1|
		^super.newCopyArgs(action, dt).init
	}
	init {
		now = lastTime = Main.elapsedTime;
	}

	filterValue { |...args|
		now = Main.elapsedTime;
		delta = now - lastTime;

		if (delta >= dt) {
			action.value(*args);
			lastTime = now;
			^this
		};
		// do it later:
		latestArgs = args;
		if (scheduled.not) {
			scheduled = true;
			SystemClock.sched(dt - delta, {
				action.value(*latestArgs);
				lastTime = Main.elapsedTime;
				scheduled = false;
			})
		}
	}
}