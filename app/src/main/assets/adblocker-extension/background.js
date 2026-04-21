(function() {
  'use strict';

  var AD_DOMAINS = [
    'doubleclick.net', 'googlesyndication.com', 'googleadservices.com',
    'google-analytics.com', 'googletagmanager.com', 'googletagservices.com',
    'facebook.net', 'facebook.com/tr', 'connect.facebook.net',
    'analytics.google.com', 'adservice.google.com',
    'pagead2.googlesyndication.com', 'tpc.googlesyndication.com',
    'ad.doubleclick.net', 'stats.g.doubleclick.net',
    'amazon-adsystem.com', 'aax.amazon-adsystem.com',
    'ads.yahoo.com', 'analytics.yahoo.com',
    'ads.twitter.com', 'analytics.twitter.com',
    'ads.linkedin.com', 'bing.com/action',
    'criteo.com', 'criteo.net',
    'outbrain.com', 'taboola.com', 'mgid.com',
    'adnxs.com', 'adsrvr.org', 'rubiconproject.com',
    'pubmatic.com', 'openx.net', 'casalemedia.com',
    'sharethrough.com', 'spotxchange.com',
    'moatads.com', 'doubleverify.com', 'adsafeprotected.com',
    'scorecardresearch.com', 'quantserve.com', 'bluekai.com',
    'exelator.com', 'crwdcntrl.net', 'demdex.net',
    'baidu.com/cpro', 'pos.baidu.com', 'hm.baidu.com',
    'cpro.baidustatic.com', 'eclick.baidu.com',
    'tanx.com', 'mmstat.com', 'cnzz.com',
    'umeng.com', 'growingio.com',
    'pagead.l.doubleclick.net', 'adclick.g.doubleclick.net',
    'serving-sys.com', 'mediaplex.com',
    'popads.net', 'popcash.net', 'propellerads.com'
  ];

  var AD_PATTERNS = [
    /\/ads[\/\?\.]/i,
    /\/ad[\/\?\.]/i,
    /\/advert/i,
    /\/banner[\/\?\.]/i,
    /\/popup[\/\?\.]/i,
    /\/tracking[\/\?\.]/i,
    /\/pixel[\/\?\.]/i,
    /\/beacon[\/\?\.]/i,
    /\.gif\?.*click/i,
    /\/pagead\//i,
    /\/adserver/i,
    /\/adframe/i
  ];

  var blockedCount = 0;

  function shouldBlock(url) {
    try {
      var hostname = new URL(url).hostname;
      for (var i = 0; i < AD_DOMAINS.length; i++) {
        if (hostname === AD_DOMAINS[i] || hostname.endsWith('.' + AD_DOMAINS[i])) {
          return true;
        }
      }
      for (var j = 0; j < AD_PATTERNS.length; j++) {
        if (AD_PATTERNS[j].test(url)) {
          return true;
        }
      }
    } catch(e) {}
    return false;
  }

  browser.webRequest.onBeforeRequest.addListener(
    function(details) {
      if (shouldBlock(details.url)) {
        blockedCount++;
        browser.runtime.sendNativeMessage('browser', {
          type: 'ad_blocked',
          url: details.url,
          count: blockedCount
        });
        return { cancel: true };
      }
      return {};
    },
    { urls: ['<all_urls>'] },
    ['blocking']
  );
})();
