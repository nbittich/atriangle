import {Menu, MenuSection} from "../core/models";
import {environment} from "../../environments/environment";

const {REST_ENDPOINT, PROXY_ENDPOINT, FRONTEND_ENDPOINT} = MenuSection;
export default  [
  {
    url: environment.backendUrl + '/api/project/swagger-ui.html',
    name: 'Project',
    section: REST_ENDPOINT,
    icon: {
      pack: 'fas',
      name: 'tasks'
    }
  },
  {
    url: environment.backendUrl + '/api/xls2rdf/swagger-ui.html',
    name: 'Skos Play',
    section: REST_ENDPOINT,
    icon: {
      pack: 'far',
      name: 'file-excel'
    }
  },
  {
    url: environment.backendUrl + '/api/shacl/swagger-ui.html',
    name: 'Shacl',
    section: REST_ENDPOINT,
    icon: {
      pack: 'fas',
      name: 'check'
    }
  },
  {
    url: environment.backendUrl + '/api/sparql/swagger-ui.html',
    name: 'Sparql',
    section: REST_ENDPOINT,
    icon: {
      pack: 'fas',
      name: 'search'
    }
  },
  {
    url: environment.backendUrl + '/api/upload/swagger-ui.html',
    name: 'Upload',
    section: REST_ENDPOINT,
    icon: {
      pack: 'fas',
      name: 'upload'
    }
  },
  {
    url: environment.backendUrl + '/api/elastic/swagger-ui.html',
    name: 'Elastic',
    section: REST_ENDPOINT,
    icon: {
      pack: 'fab',
      name: 'searchengin'
    }
  },
  {
    url: environment.backendUrl + '/api/mongodb/swagger-ui.html',
    name: 'Mongo',
    section: REST_ENDPOINT,
    icon: {
      pack: 'fas',
      name: 'search-location'
    }
  },
  {
    url: environment.backendUrl + '/portainer/',
    name: 'Portainer',
    section: PROXY_ENDPOINT,
    icon: {
      pack: 'fas',
      name: 'users-cog'
    }
  },
  {
    url: environment.backendUrl + '/proxy/elasticsearch/',
    name: 'ElasticSearch',
    section: PROXY_ENDPOINT,
    icon: {
      pack: 'fab',
      name: 'searchengin'
    }
  },
  {
    url: environment.backendUrl + '/proxy/mongo/',
    name: 'MongoDB',
    section: PROXY_ENDPOINT,
    icon: {
      pack: 'fas',
      name: 'search-location'
    }
  },
  {
    url: environment.backendUrl + '/proxy/sparql-endpoint/',
    name: 'BlazeGraph',
    section: PROXY_ENDPOINT,
    icon: {
      pack: 'fas',
      name: 'search'
    }
  },
  {
    url: 'projects',
    name: 'Projects',
    section: FRONTEND_ENDPOINT,
    icon: {
      pack: 'fas',
      name: 'project-diagram'
    }
  }
] as Menu[];

