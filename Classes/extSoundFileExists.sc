// SoundFile.pathMatch(Platform.resourceDir +/+ "*/*");
/*

*/

+ SoundFile {
	*pathMatch { |path|
		^path.pathMatch.select(this.exists(_))
	}
	*exists { |path|
		var sf = this.new(path);
		var exists = sf.openRead;
		if (exists) { sf.close };
		^exists
	}
}



