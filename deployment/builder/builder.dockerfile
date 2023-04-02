ARG builder_image=project-planning/builder

FROM $builder_image

ENV PROJECT_NAME project-planning

ENV WORKSPACE_DIR /workspace
ENV SRC_DIR $WORKSPACE_DIR/src
ENV BUILD_DIR $WORKSPACE_DIR/build

ARG GRADLE_TASK=distribution

RUN mkdir -p $SRC_DIR && mkdir -p $BUILD_DIR

RUN echo "##teamcity[blockOpened name='Init build dir state before' description='Init build dir ($BUILD_DIR) state before source copy (except node_modules)']" \
    && find $BUILD_DIR | egrep -v '/node_modules/' \
    && echo "##teamcity[blockClosed name='Init build dir state before']" \
    \
    && echo "##teamcity[blockOpened name='Init source dir state before' description='Init source dir ($SRC_DIR) state before source copy']" \
    && find $SRC_DIR | egrep -v '/node_modules/' \
    && echo "##teamcity[blockClosed name='Init source dir state before']"

COPY . $SRC_DIR

WORKDIR $WORKSPACE_DIR

RUN echo "##teamcity[blockOpened name='Source dir state after COPY command' description='Source dir ($SRC_DIR) state after COPY command']" \
    && find $SRC_DIR | egrep -v '/node_modules/' \
    && echo "##teamcity[blockClosed name='Source dir state after COPY command']" \
    \
    && echo "##teamcity[blockOpened name='Copy source to build dir' description='Copy from src ($SRC_DIR) to build ($BUILD_DIR)']" \
    && rsync -ahP $SRC_DIR/ $BUILD_DIR/ \
    && echo "##teamcity[blockClosed name='Copy source to build dir']" \
    \
    && echo "##teamcity[blockOpened name='Build dir state after copy sources' description='Build dir ($BUILD_DIR) state after source copy (except node_modules)']" \
    && find $BUILD_DIR | egrep -v '/node_modules/' \
    && echo "##teamcity[blockClosed name='Build dir state after copy sources']"

WORKDIR $BUILD_DIR

RUN echo "##teamcity[blockOpened name='gradleTask' description='Gradle Run Task ($GRADLE_TASK)']" \
    && ./gradlew --info --no-daemon --build-cache $GRADLE_TASK \
    && echo "##teamcity[blockClosed name='gradleTask']" \
    \
    && echo "##teamcity[blockOpened name='cleanBuildDir' description='Clean build dir for the next builds']" \
    && find -type f | egrep -v '/(node_modules|.gradle|build)/' | xargs -I {} rm {} \
    && echo "##teamcity[blockClosed name='cleanBuildDir']" \
    \
    && echo "##teamcity[removeOriginalSources 'Remove original sources in $SRC_DIR']" \
    && rm -rf $SRC_DIR

