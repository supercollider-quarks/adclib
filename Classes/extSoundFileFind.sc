///// find 4 default sounds in SC resourceDir:
// SoundFile.pathMatch(Platform.resourceDir +/+ "sounds/a11*.aiff");
// SoundFile.pathMatch("~/*/*.*").size;
// SoundFile.pathMatch("~/*/*/*.*").size;

/*

// three intended fails:
SoundFile.exists("nope/no/file/there.wav");
SoundFile.exists("this/is/a/folder/");
SoundFile.exists("this/is/a/forbidden.psd");
// psd open bug says something
SoundFile.openRead("/Users/adc/Pictures/DragonFyrAll.psd");
SoundFile.exists("/Users/adc/Pictures/DragonFyrAll.psd");

// true:
SoundFile.exists(Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff");

SoundFile.openRead("/Users/adc/Pictures/DragonFyrAll.psd")
SoundFile.pathMatch("/Users/adc/Pictures/DragonFyrAll.psd")
SoundFile.pathMatch(Platform.resourceDir +/+ "sounds/a11*.aiff");
SoundFile.find([Platform.resourceDir +/+ "sounds/a11*.aiff"]).flat;
SoundFile.find(Platform.resourceDir +/+ "sounds/""*").flat;

SoundFile.find(Platform.resourceDir +/+ "sounds/""*", { |sf| sf.duration > 1 }).flat;
SoundFile.find(Platform.resourceDir +/+ "sounds/""*", { |sf| sf.duration <= 1 }).flat;

*/

+ SoundFile {

	// find all soundfiles at path patterns
	// that pass the test:
	*find { |paths, test = true|
		if (paths.isKindOf(String)) { paths = paths.bubble };
		^paths.collect { |pathpat|
			pathpat.pathMatch.collect { |path|
				SoundFile(path);
			}.select { |sndfile|
				var wasFound = sndfile.openRead and: {
					test.value(sndfile)
				};
				if (sndfile.isOpen) { sndfile.close };
				wasFound
			}
		}
	}

	*pathMatch { |path|
		^path.pathMatch.select(this.exists(_))
	}

	*exists { |path, postExcluded = false|
		var sf, exists;
		sf = this.new(path);
		exists = sf.openRead(path, postExcluded);
		if (sf.isOpen) { sf.close };
		^exists
	}
}
