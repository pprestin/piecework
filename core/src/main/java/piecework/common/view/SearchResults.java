package piecework.common.view;

import java.io.Serializable;
import java.util.*;

import javax.xml.bind.annotation.*;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import piecework.model.*;
import piecework.model.Process;
import piecework.util.ManyMap;

@XmlRootElement(name = SearchResults.Constants.ROOT_ELEMENT_NAME)
@XmlType(name = SearchResults.Constants.TYPE_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({Button.class, Constraint.class, Field.class, FormValue.class, Interaction.class, Process.class, ProcessInstance.class, QueryParameter.class, Screen.class, Section.class, Task.class})
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResults implements Serializable {

	private static final long serialVersionUID = -7773190153155500335L;
	
	@XmlElement
	private final String resourceName;

	@XmlElement
	private final String resourceLabel;

	@XmlElement
	private final Integer firstResult;

    @XmlElement
	private final Integer maxResults;

    @XmlElement
	private final Integer total;

    @XmlTransient
    @JsonIgnore
    private final Integer page;

    @XmlTransient
    @JsonIgnore
	private final Boolean moreResults;

    @XmlTransient
    @JsonIgnore
	private final Boolean incomplete;

    @XmlTransient
    @JsonIgnore
	private final String datePerspective;

    @XmlTransient
    @JsonIgnore
	private final String from;

    @XmlTransient
    @JsonIgnore
	private final String to;
	
	@XmlElementWrapper(name="list")
	@XmlElement(name="item")
	private final List<Object> list;
	
	@XmlElementWrapper(name="definitions")
	@XmlElement(name="definition")
	protected List<Object> definitions;

    @XmlElementWrapper(name="parameters")
    @XmlElementRef
    private final List<QueryParameter> parameters;

    @XmlAttribute
    private final String link;
	
	private SearchResults() {
		this(new SearchResults.Builder(), new ViewContext());
	}
			
	private SearchResults(SearchResults.Builder builder, ViewContext context) {
		this.list = builder.list;
		this.definitions = builder.definitions;
		this.resourceName = builder.resourceName;
		this.resourceLabel = builder.resourceLabel;
		this.firstResult = builder.firstResult;
		this.maxResults = builder.maxResults;
		this.total = builder.total;
		this.moreResults = builder.moreResults;
		this.incomplete = builder.incomplete;
		this.page = firstResult != null && maxResults != null && maxResults.intValue() > 0 ? firstResult.intValue() / maxResults.intValue() + 1 : 1;
		this.datePerspective = builder.datePerspective;
		this.from = builder.from;
		this.to = builder.to;
        this.link = builder.link;
        this.parameters = Collections.unmodifiableList(builder.getParameters());
	}
	
	public List<Object> getList() {
		return list;
	}


	public Integer getTotal() {
		return total;
	}

	public Integer getPage() {
		return page;
	}

	public String getDatePerspective() {
		return datePerspective;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getResourceLabel() {
		return resourceLabel;
	}
	
	public List<Object> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<Object> definitions) {
		this.definitions = definitions;
	}

	public Boolean getMoreResults() {
		return moreResults;
	}

	public Boolean getIncomplete() {
		return incomplete;
	}

    public Integer getFirstResult() {
        return firstResult;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public String getLink() {
        return link;
    }

    public List<QueryParameter> getParameters() {
        return parameters;
    }

    static class Attributes {
        final static String URI = "link";
    }
	
	static class Constants {
		public static final String ROOT_ELEMENT_NAME = "searchResults";
		public static final String TYPE_NAME = "SearchResultsType";
	}

	public String getResourceName() {
		return resourceName;
	}

	public final static class Builder {

		private String resourceName;
		private String resourceLabel;
		private List<Object> definitions;
		private List<Object> list;
		private Integer firstResult;
		private Integer maxResults;
		private Integer total;
		private Integer page;
		private Boolean moreResults;
		private Boolean incomplete;
		private String datePerspective;
		private String from;
		private String to;
        private String link;
        private ManyMap<String, String> parameters;
		
		public Builder() {
			super();
            this.parameters = new ManyMap<String, String>();
		}
		
		public SearchResults build() {
			return new SearchResults(this, null);
		}
		
		public SearchResults build(ViewContext context) {
			return new SearchResults(this, context);
		}
		
		public Builder resourceName(String resourceName) {
			this.resourceName = resourceName;
			return this;
		}
		
		public Builder resourceLabel(String resourceLabel) {
			this.resourceLabel = resourceLabel;
			return this;
		}
		
		public Builder definition(Object definition) {
			if (this.definitions == null)
				this.definitions = new ArrayList<Object>();
			this.definitions.add(definition);
			return this;
		}
		
		public Builder item(Object item) {
			if (this.list == null)
				this.list = new ArrayList<Object>();
			this.list.add(item);
			return this;
		}
		
		public Builder items(List<?> items) {
			if (this.list == null)
				this.list = new ArrayList<Object>();
			this.list.addAll(items);
			return this;
		}

        public Builder parameter(String name, String... values) {
            if (values.length > 0) {
                for (String value : values)
                    this.parameters.putOne(name, value);
            }
            return this;
        }
		
		public Builder firstResult(Integer firstResult) {
			this.firstResult = firstResult;
			return this;
		}
		
		public Builder maxResults(Integer maxResults) {
			this.maxResults = maxResults;
			return this;
		}
		
		public Builder total(Integer total) {
			this.total = total;
			return this;
		}
		
		public Builder page(Integer page) {
			this.page = page;
			return this;
		}
		
		public Builder moreResults(Boolean moreResults) {
			this.moreResults = moreResults;
			return this;
		}
		
		public Builder incomplete(Boolean incomplete) {
			this.incomplete = incomplete;
			return this;
		}
		
		public Builder datePerspective(String datePerspective) {
			this.datePerspective = datePerspective;
			return this;
		}
		
		public Builder from(String from) {
			this.from = from;
			return this;
		}
		
		public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder link(String link) {
            this.link = link;
            return this;
        }

        public List<QueryParameter> getParameters() {
            List<QueryParameter> parameters = new ArrayList<QueryParameter>();
            for (Map.Entry<String, List<String>> entry : this.parameters.entrySet()) {
                 parameters.add(new QueryParameter.Builder().name(entry.getKey()).values(entry.getValue()).build());
            }
            return parameters;
        }
	}
}
