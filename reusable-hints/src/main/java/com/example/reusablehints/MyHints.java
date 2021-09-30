package com.example.reusablehints;

import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.type.HintDeclaration;
import org.springframework.nativex.type.NativeConfiguration;
import org.springframework.nativex.type.ResourcesDescriptor;
import org.springframework.nativex.type.TypeSystem;

import java.util.List;


@NativeHint(
	options = {"--enable-http", "--enable-https"}
//	resources = {@ResourceHint(patterns = "/sa.properties")},
)
public class MyHints implements NativeConfiguration {


	@Override
	public List<HintDeclaration> computeHints(TypeSystem typeSystem) {
		var hd = new HintDeclaration();
		System.out.println("running this hint!");
		hd.addResourcesDescriptor(new ResourcesDescriptor(new String[]{"/log4j2.springboot"},
			false));
		return List.of(hd);
	}

	@Override
	public boolean isValid(TypeSystem typeSystem) {
		return true;
	}
}
