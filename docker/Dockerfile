FROM centos:7.5.1804
COPY jre-10.0.1_linux-x64_bin.tar.gz /tmp
COPY WebCrawler-1.0-SNAPSHOT-jar-with-dependencies.jar /tmp

RUN set -x \
	&& groupadd -g 1000 webcrawler \
	&& useradd -m -d /opt/webcrawler -s /bin/bash -u 1000 -g webcrawler webcrawler \
	&& tar -xzf /tmp/jre-10.0.1_linux-x64_bin.tar.gz -C /opt/ \
	&& ln -s /opt/jre-10.0.1 /opt/jre \
	&& mv /tmp/WebCrawler-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/webcrawler \
	&& chown webcrawler:webcrawler /opt/webcrawler/WebCrawler-1.0-SNAPSHOT-jar-with-dependencies.jar \
	&& rm -f /tmp/jre-10.0.1_linux-x64_bin.tar.gz 

ENTRYPOINT ["/opt/jre/bin/java"]
CMD ["-jar","/opt/webcrawler/WebCrawler-1.0-SNAPSHOT-jar-with-dependencies.jar"]
