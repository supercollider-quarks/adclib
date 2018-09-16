/*
(
a = nil;
{ a == 2 }.whenTrueWithin(2, { "done".postln }, { "failed".postln });
fork { 1.99.wait; a = 2 };
)

(
{ s.serverRunning }.whenTrueWithin(3,
	{ "YES! doWhenBooted...".postln; ().play },
	{ "NO! boot timed out - quitting again.".postln; s.quit }
);
fork { s.quit; 1.wait; s.boot };
)
*/

+ Function {
	whenTrueWithin { |timeout = 3, thenFunc, elseFunc, dt = (1/16)|
		var remaining = timeout;
		^Routine {
			while {
				remaining = remaining - dt;
				this.value.not and: { remaining >= 0 }
			} {
				dt.wait;
			};
			if (this.value, thenFunc, elseFunc)
		}.play(SystemClock)
	}
}
