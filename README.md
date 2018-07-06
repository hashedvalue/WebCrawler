# Web Crawler

### Purpose
Provided software crawls given starting http or https url and provides a summary of found links afterwards. Links are divided into 4 groups:
- internal links: links which are in the same domain as starting link or in its subdomain
- external links: links in other domains
- internal static links: links to static content related to starting link
- external static links: links to static content not related to starting link

### Build
Provided software uses maven with assembly plugin for build. To create a fat 'all-in-one' jar similar to the one in docker/ subdirectory please run:
```mvn package```

To create a docker container with already available fat jar please run:
```cd docker; docker build -t webcrawler:1.0 .```

### Environment variables
- ```CWR_MAX_PROCESSED_PAGES```: maximum number of processed urls, after reaching this limit crawler will stop (example: 20, default: 10)
- ```CWR_REPORT_FILENAME```: destination path for report inside the container, this path must be mapped to the host so that report is available after docker run (example: /tmp/report.xml, default: N/A)
- ```CWR_START_URL```: starting http or https url (example: http://dilbert.com, default: N/A)
- ```CWR_USER_AGENT```: User-Agent header for jsoup requests (default: "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1)
- ```CWR_VALIDATE_SSL_CERTS```: in rare cases there may be need to disable checks of SSL certificates, USE THIS WITH CARE! (example: true, default: true)
- ```HTTP_PROXY```: provides http proxy endpoint (example: http://127.0.0.1:3128, example: N/A)
- ```HTTPS_PROXY```: provides https proxy endpoint (example: http://127.0.0.1:3128, example: N/A)

### Execution
Example docker run without proxy:
```mkdir -p /tmp/webcrawler; docker run --net host -v /tmp/webcrawler:/tmp --rm -e CWR_MAX_PROCESSED_PAGES="20" -e CWR_REPORT_FILENAME="/tmp/dilbert.com.xml" -e CWR_START_URL="http://dilbert.com" webcrawler:1.0```: afterwards your report will be available in /tmp/webcrawler/dilbert.com.xml file

Example docker run with proxy and disabled verification of SSL certificates (USE THIS WITH CARE!):
```mkdir -p /tmp/webcrawler; docker run --net host -v /tmp/webcrawler:/tmp --rm -e CWR_MAX_PROCESSED_PAGES="20" -e CWR_REPORT_FILENAME="/tmp/dilbert.com.xml" -e CWR_START_URL="http://dilbert.com" -e "HTTP_PROXY=http://127.0.0.1:3128" -e "HTTPS_PROXY=http://127.0.0.1:3128" -e "CWR_VALIDATE_SSL_CERTS=false" webcrawler:1.0```: afterwards your report will be available in /tmp/webcrawler/dilbert.com.xml file

Example standalone jar run:
```mkdir -p /tmp/webcrawler; CWR_MAX_PROCESSED_PAGES="20" CWR_REPORT_FILENAME="/tmp/dilbert.com.xml" CWR_START_URL="http://dilbert.com" java -jar WebCrawler-1.0-SNAPSHOT-jar-with-dependencies.jar```: afterwards your report will be available in /tmp/webcrawler/dilbert.com.xml file
