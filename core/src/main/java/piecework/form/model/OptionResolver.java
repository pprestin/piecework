package piecework.form.model;

import java.util.List;

public interface OptionResolver {

	List<Option> getOptions(String type, OptionProvider<?> contract);
	
}