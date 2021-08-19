
Mem {
	classvar <>minSize = 3;
	var <>maxMems, <used;

	*new { |maxMems = 50|
		^this.newCopyArgs(maxMems, List[])
	}

	add { |item|
		// remove if already there
		used.remove(item);
		// addfirst,
		used = used.addFirst(item);
		// drop oldest one(s) if maxsize reached
		while { used.size > maxMems } { used.pop }
	}

	filter { |list, minSize|
		var res = list.copy;
		minSize = minSize ? this.class.minSize;
		used.do { |item|
			if (res.size > minSize) {
				res.remove(item)
			} {
				^res
			}
		};
		^res
	}

	filterDict { |dict, minSize|
		var res = dict.copy;
		minSize = minSize ? this.class.minSize;
		used.do { |key|
			if (res.size > minSize) {
				res.removeAt(key)
			} {
				^res
			}
		};
		^res
	}
}
