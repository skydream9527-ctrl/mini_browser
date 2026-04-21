(function() {
  'use strict';

  var VIDEO_EXTENSIONS = ['.mp4', '.m3u8', '.flv', '.webm', '.mpd', '.ts', '.mkv', '.avi'];
  var VIDEO_MIME_TYPES = [
    'video/', 'application/x-mpegurl', 'application/vnd.apple.mpegurl',
    'application/dash+xml', 'application/x-mpegURL'
  ];

  var sniffedUrls = new Set();

  function isVideoUrl(url) {
    var lower = url.toLowerCase().split('?')[0];
    return VIDEO_EXTENSIONS.some(function(ext) { return lower.endsWith(ext); });
  }

  function getVideoType(url) {
    var lower = url.toLowerCase().split('?')[0];
    if (lower.endsWith('.m3u8')) return 'M3U8';
    if (lower.endsWith('.mpd')) return 'DASH';
    if (lower.endsWith('.mp4')) return 'MP4';
    if (lower.endsWith('.webm')) return 'WEBM';
    if (lower.endsWith('.flv')) return 'FLV';
    if (lower.endsWith('.ts')) return 'TS';
    return 'OTHER';
  }

  browser.webRequest.onBeforeRequest.addListener(
    function(details) {
      if (sniffedUrls.has(details.url)) return;
      if (!isVideoUrl(details.url)) return;
      if (details.url.includes('.ts') && sniffedUrls.size > 0) return;

      sniffedUrls.add(details.url);
      browser.runtime.sendNativeMessage('browser', {
        type: 'video_sniffed',
        url: details.url,
        videoType: getVideoType(details.url),
        source: 'network-request',
        tabId: details.tabId
      });
    },
    { urls: ['<all_urls>'] },
    []
  );

  browser.runtime.onMessage.addListener(function(message, sender) {
    if (message.type === 'video_found') {
      if (sniffedUrls.has(message.url)) return;
      sniffedUrls.add(message.url);

      browser.runtime.sendNativeMessage('browser', {
        type: 'video_sniffed',
        url: message.url,
        videoType: getVideoType(message.url),
        source: message.source || 'dom',
        pageUrl: message.pageUrl || '',
        pageTitle: message.pageTitle || '',
        tabId: sender.tab ? sender.tab.id : -1
      });
    }
  });

  browser.webRequest.onHeadersReceived.addListener(
    function(details) {
      if (sniffedUrls.has(details.url)) return;

      var headers = details.responseHeaders || [];
      for (var i = 0; i < headers.length; i++) {
        if (headers[i].name.toLowerCase() === 'content-type') {
          var ct = headers[i].value.toLowerCase();
          var isVideo = VIDEO_MIME_TYPES.some(function(mt) { return ct.indexOf(mt) !== -1; });
          if (isVideo) {
            sniffedUrls.add(details.url);
            browser.runtime.sendNativeMessage('browser', {
              type: 'video_sniffed',
              url: details.url,
              videoType: ct.indexOf('mpegurl') !== -1 ? 'M3U8' :
                         ct.indexOf('dash') !== -1 ? 'DASH' : 'MP4',
              source: 'network-mime',
              tabId: details.tabId
            });
          }
          break;
        }
      }
    },
    { urls: ['<all_urls>'] },
    ['responseHeaders']
  );
})();
