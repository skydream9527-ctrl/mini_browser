(function() {
  'use strict';

  const SENT_URLS = new Set();

  function sendVideo(url, source) {
    if (!url || SENT_URLS.has(url)) return;
    if (url.startsWith('blob:') || url.startsWith('data:')) return;
    SENT_URLS.add(url);
    browser.runtime.sendMessage({
      type: 'video_found',
      url: url,
      source: source,
      pageUrl: window.location.href,
      pageTitle: document.title
    });
  }

  function scanVideoElement(el) {
    if (el.src) sendVideo(el.src, 'dom-video-src');
    if (el.currentSrc) sendVideo(el.currentSrc, 'dom-video-currentSrc');
    el.querySelectorAll('source').forEach(function(source) {
      if (source.src) sendVideo(source.src, 'dom-source-tag');
    });
  }

  function scanPage() {
    document.querySelectorAll('video').forEach(scanVideoElement);
    document.querySelectorAll('iframe').forEach(function(iframe) {
      try {
        var iframeDoc = iframe.contentDocument;
        if (iframeDoc) {
          iframeDoc.querySelectorAll('video').forEach(scanVideoElement);
        }
      } catch(e) {}
    });
  }

  scanPage();

  var observer = new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
      mutation.addedNodes.forEach(function(node) {
        if (node.nodeType !== 1) return;
        if (node.tagName === 'VIDEO') {
          scanVideoElement(node);
        } else if (node.querySelectorAll) {
          node.querySelectorAll('video').forEach(scanVideoElement);
        }
      });
    });
  });

  observer.observe(document.documentElement, {
    childList: true,
    subtree: true
  });

  var origSrc = Object.getOwnPropertyDescriptor(HTMLMediaElement.prototype, 'src');
  if (origSrc && origSrc.set) {
    Object.defineProperty(HTMLMediaElement.prototype, 'src', {
      set: function(val) {
        origSrc.set.call(this, val);
        if (this.tagName === 'VIDEO') {
          setTimeout(function() { sendVideo(val, 'dom-src-setter'); }, 100);
        }
      },
      get: origSrc.get,
      configurable: true
    });
  }
})();
